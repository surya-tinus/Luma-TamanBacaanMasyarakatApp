package com.example.projectgroup7

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.compose.ui.semantics.text
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.preference.isNotEmpty
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
import java.util.Calendar

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
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
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

        val chipGroup = view.findViewById<ChipGroup>(R.id.layout_categories)
        chipGroup.addOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedId = checkedIds.first()
                val chip = group.findViewById<Chip>(checkedId)
                chip?.let {
                    val category = it.text.toString().lowercase()
                    fetchBooks(category = category)
                }
            }
        }

        view.findViewById<Button>(R.id.btn_explore).setOnClickListener {
            fetchBooks(category = "fiction")
        }

        // Load default data
        fetchBooks(category = "romance")
        fetchBooks(isNewest = true)
    }

    private fun openDetail(selectedBook: Book) {
        val bundle = bundleOf(
            "title" to selectedBook.title,
            "category" to selectedBook.category,
            "imageUrl" to selectedBook.imageUrl,
            "description" to selectedBook.description
        )
        requireView().findNavController()
            .navigate(R.id.action_homeFragment_to_bookDetailFragment, bundle)
    }

    private fun fetchBooks(category: String? = null, isNewest: Boolean = false) {
        val query = when {
            isNewest -> "newest"
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
                        val volumeInfo = item.volumeInfo

                        val title = volumeInfo.title ?: "Unknown Title"
                        val categoryName = volumeInfo.categories?.firstOrNull() ?: "Unknown"
                        val imageUrl = volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
                        val description = volumeInfo.description ?: "Tidak ada deskripsi."

                        list.add(Book(title, categoryName, imageUrl, description))
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
