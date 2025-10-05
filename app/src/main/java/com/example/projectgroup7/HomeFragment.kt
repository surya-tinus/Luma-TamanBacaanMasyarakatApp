package com.example.projectgroup7

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectgroup7.model.Book
import com.example.projectgroup7.model.BookResponse
import com.example.projectgroup7.network.RetrofitClient
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var rvCategory: RecyclerView
    private lateinit var rvNewest: RecyclerView
    private lateinit var categoryAdapter: BookAdapter
    private lateinit var newestAdapter: BookAdapter
    private val categoryList = mutableListOf<Book>()
    private val newestList = mutableListOf<Book>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val greetingTextView = view.findViewById<TextView>(R.id.tv_greeting)
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        greetingTextView.text = when (currentHour) {
            in 5..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            in 17..20 -> "Good Evening,"
            else -> "Good Night,"
        }

        // Recycler setup
        rvCategory = view.findViewById(R.id.rv_category_books)
        rvNewest = view.findViewById(R.id.rv_new_books)

        rvCategory.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvNewest.layoutManager = GridLayoutManager(requireContext(), 3)

        categoryAdapter = BookAdapter(categoryList) { openDetail(it) }
        newestAdapter = BookAdapter(newestList) { openDetail(it) }

        rvCategory.adapter = categoryAdapter
        rvNewest.adapter = newestAdapter

        // ChipGroup listener (kategori dinamis)
        val chipGroup = view.findViewById<ChipGroup>(R.id.layout_categories)
        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1) {
                val chip = group.findViewById<Chip>(checkedId)
                val category = chip.text.toString().lowercase()
                fetchBooks(category = category)
            }
        }

        // Explore All button
        // Explore All button -> navigasi ke ExploreFragment
        view.findViewById<Button>(R.id.btn_explore).setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_homeFragment_to_exploreFragment)
        }


        // Load default data
        fetchBooks(category = "romance") // default
        fetchBooks(isNewest = true)
    }

    private fun openDetail(selectedBook: Book) {
        val bundle = Bundle().apply {
            putString("title", selectedBook.title)
            putString("category", selectedBook.category)
            putString("image", selectedBook.imageUrl)
        }
        requireView().findNavController()
            .navigate(R.id.action_homeFragment_to_bookDetailFragment, bundle)
    }

    private fun fetchBooks(category: String? = null, isNewest: Boolean = false) {
        val query = when {
            isNewest -> "fiction"
            category != null -> "subject:$category"
            else -> "fiction"
        }

        RetrofitClient.instance.searchBooks(query).enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                if (response.isSuccessful) {
                    val items = response.body()?.items
                    val list = if (isNewest) newestList else categoryList
                    val adapter = if (isNewest) newestAdapter else categoryAdapter

                    list.clear()
                    items?.forEach { item ->
                        val title = item.volumeInfo.title ?: "Unknown Title"
                        val categoryName = item.volumeInfo.categories?.firstOrNull() ?: "Unknown"
                        val imageUrl = item.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://") ?: ""
                        list.add(Book(title, categoryName, imageUrl))
                    }

                    adapter.notifyDataSetChanged()
                } else {
                    Log.e("HomeFragment", "Response failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                Log.e("HomeFragment", "Error fetching books: ${t.message}")
            }
        })
    }

}
