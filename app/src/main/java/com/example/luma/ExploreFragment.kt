package com.example.luma

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luma.model.Book
import com.example.luma.model.BookResponse
import com.example.luma.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExploreFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookAdapter
    private val bookList = mutableListOf<Book>()

    // daftar kategori populer yang akan ditampilkan
    private val categories = listOf(
        "fiction", "romance", "biography", "science", "fantasy"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewExplore)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = BookAdapter(bookList) { openDetail(it) }
        recyclerView.adapter = adapter

        // Fetch semua kategori
        categories.forEach { category ->
            fetchBooks(category)
        }

        return view
    }

    private fun openDetail(selectedBook: Book) {
        val bundle = Bundle().apply {
            putString("title", selectedBook.title)
            putString("category", selectedBook.category)
            putString("image", selectedBook.imageUrl)
        }
        requireView().findNavController()
            .navigate(R.id.action_exploreFragment_to_bookDetailFragment, bundle)
    }

    private fun fetchBooks(category: String) {
        val query = "subject:$category"

        RetrofitClient.instance.searchBooks(query).enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                if (response.isSuccessful) {
                    val items = response.body()?.items
                    items?.forEach { item ->
                        val title = item.volumeInfo.title ?: "Unknown Title"
                        val categoryName = item.volumeInfo.categories?.firstOrNull() ?: category
                        val imageUrl = item.volumeInfo.imageLinks?.thumbnail
                            ?.replace("http://", "https://") ?: ""

                        bookList.add(Book(title, categoryName, imageUrl, ""))
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e("ExploreFragment", "Response failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                Log.e("ExploreFragment", "Error fetching books: ${t.message}")
            }
        })
    }
}