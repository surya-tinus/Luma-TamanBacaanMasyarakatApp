package com.example.luma

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
// ======================================================
// PERBAIKAN 1: Pastikan menggunakan 'android.widget.SearchView'
// ======================================================
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luma.model.Book
import com.example.luma.model.BookResponse
import com.example.luma.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var bookAdapter: BookAdapter
    private var bookList = mutableListOf<Book>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = view.findViewById(R.id.searchView)
        recyclerView = view.findViewById(R.id.recyclerViewBooks)
        progressBar = view.findViewById(R.id.progressBar)

        setupRecyclerView()
        setupSearchView()
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(bookList) { selectedBook ->
            val bundle = bundleOf(
                "title" to selectedBook.title,
                "category" to selectedBook.category,
                "imageUrl" to selectedBook.imageUrl,
                "description" to selectedBook.description
            )
            findNavController().navigate(R.id.action_searchFragment_to_bookDetailFragment, bundle)
        }
        recyclerView.adapter = bookAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSearchView() {
        // ======================================================
        // PERBAIKAN 2: Konfigurasi tambahan untuk memastikan SearchView aktif
        // ======================================================
        // Mengatur agar search view tidak dalam mode ikon (langsung berupa kolom teks)
        searchView.isIconified = false
        // Secara opsional, bisa juga langsung membuka keyboard saat fragmen dibuka
        // searchView.requestFocus()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Metode ini dipanggil saat user menekan tombol 'search' di keyboard
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    searchBooks(query)
                    // Menutup keyboard agar tidak mengganggu tampilan hasil
                    searchView.clearFocus()
                }
                return true // Mengindikasikan bahwa event sudah ditangani
            }

            // Metode ini dipanggil setiap kali teks di search view berubah
            override fun onQueryTextChange(newText: String?): Boolean {
                // Jika Anda ingin hasil pencarian muncul saat mengetik,
                // Anda bisa memanggil searchBooks() di sini.
                // Untuk sekarang, kita biarkan false.
                return false
            }
        })
    }

    private fun searchBooks(query: String) {
        showLoading(true)

        RetrofitClient.instance.searchBooks(query).enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    bookList.clear()

                    val itemsFromApi = response.body()?.items ?: emptyList()
                    val mappedBooks = itemsFromApi.map { bookItem ->
                        val volumeInfo = bookItem.volumeInfo
                        Book(
                            title = volumeInfo.title,
                            category = volumeInfo.categories?.firstOrNull() ?: "Tidak Berkategori",
                            imageUrl = volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://"),
                            description = volumeInfo.description ?: "Tidak ada deskripsi."
                        )
                    }
                    bookList.addAll(mappedBooks)
                    bookAdapter.notifyDataSetChanged()
                } else {
                    Log.e("SearchFragment", "API Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                showLoading(false)
                Log.e("SearchFragment", "Network Failure: ${t.message}", t)
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
