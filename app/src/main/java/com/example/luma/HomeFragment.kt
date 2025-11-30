package com.example.luma

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luma.model.Book
import com.example.luma.model.BookResponse
import com.example.luma.network.RetrofitClient
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import kotlin.collections.isNotEmpty
import android.content.Context
import androidx.recyclerview.widget.LinearSnapHelper


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
        val usernameGreeting = view.findViewById<TextView>(R.id.username_greeting)
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "User123456")

        greetingTextView.text = getGreeting()
        usernameGreeting.text = username

        // --- RecyclerView setup ---

        // Category RecyclerView (horizontal) dengan snap per item
        rvCategory = view.findViewById(R.id.rv_category_books)
        rvCategory.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val categorySnap = LinearSnapHelper() // SnapHelper bikin scroll nyantol per item
        categorySnap.attachToRecyclerView(rvCategory)
        categoryAdapter = BookAdapter(categoryList) { openDetail(it) }
        rvCategory.adapter = categoryAdapter

        // Newest RecyclerView (grid) height wrap_content + scroll dikontrol parent ScrollView
        rvNewest = view.findViewById(R.id.rv_new_books)
        rvNewest.layoutManager = object : GridLayoutManager(requireContext(), 3) {

        }
        newestAdapter = BookAdapter(newestList) { openDetail(it) }
        rvNewest.adapter = newestAdapter

        // ChipGroup listener (kategori dinamis)
        val chipGroup = view.findViewById<ChipGroup>(R.id.layout_categories)
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds.first())
                chip?.let {
                    fetchBooks(category = it.text.toString().lowercase())
                }
            }
        }

        // Explore button
        view.findViewById<Button>(R.id.btn_explore).setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_homeFragment_to_exploreFragment)
        }

        // Load default
        fetchBooks(category = "romance")
        fetchBooks(isNewest = true)
    }

    private fun getGreeting(): String {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (currentHour) {
            in 5..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            in 17..20 -> "Good Evening,"
            else -> "Good Night,"
        }
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
        val query = category?.let { "subject:$it" } ?: "fiction"
        val orderBy = if (isNewest) "newest" else null

        RetrofitClient.instance.searchBooks(query, orderBy).enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                if (response.isSuccessful) {
                    val items = response.body()?.items
                    val list = if (isNewest) newestList else categoryList
                    val adapter = if (isNewest) newestAdapter else categoryAdapter

                    list.clear()
                    items?.forEach { item ->
                        val vol = item.volumeInfo
                        val title = vol.title ?: "Unknown Title"
                        val cat = vol.categories?.firstOrNull() ?: "Unknown"
                        val img = vol.imageLinks?.thumbnail?.replace("http://", "https://")
                        val desc = vol.description ?: "Tidak ada deskripsi."
                        list.add(Book(title, cat, img, desc))
                    }
                    adapter.notifyDataSetChanged()
                } else Log.e("HomeFragment", "Response failed: ${response.code()}")
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                Log.e("HomeFragment", "Error fetching books: ${t.message}")
            }
        })
    }
}

