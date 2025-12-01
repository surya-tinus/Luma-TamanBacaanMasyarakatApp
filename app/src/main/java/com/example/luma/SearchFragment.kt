package com.example.luma

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luma.database.Book // 1. Pakai Book Database
import com.example.luma.database.viewmodels.BookViewModel // 2. Pakai ViewModel

class SearchFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var bookAdapter: BookAdapter
    private lateinit var bookViewModel: BookViewModel

    // Menyimpan daftar lengkap buku untuk difilter
    private var fullBookList = listOf<Book>()

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

        // 1. Setup ViewModel
        bookViewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]

        // 2. Setup RecyclerView
        setupRecyclerView()

        // 3. Observasi Data Buku (Ambil semua buku dulu)
        showLoading(true)
        bookViewModel.allBooks.observe(viewLifecycleOwner) { books ->
            showLoading(false)
            fullBookList = books // Simpan copy lengkapnya
            // Awalnya kosongkan list, atau tampilkan semua (sesuai selera)
            // Di sini saya kosongkan dulu sesuai request kamu (kosong kalau ga disearch)
            bookAdapter.updateData(emptyList())
        }

        // 4. Setup Search Listener
        setupSearchView()
    }

    private fun setupRecyclerView() {
        // Inisialisasi dengan list kosong
        bookAdapter = BookAdapter(emptyList()) { selectedBook ->
            // --- NAVIGASI KE DETAIL ---
            // Sesuaikan key bundle dengan apa yang diterima DetailFragment nanti
            val bundle = bundleOf(
                "title" to selectedBook.title,
                "category" to selectedBook.category,
                "imagePath" to selectedBook.imagePath, // Dulu imageUrl
                "synopsis" to selectedBook.synopsis      // Dulu description
            )
            // Pastikan ID action di nav_graph sudah benar
            findNavController().navigate(R.id.action_searchFragment_to_bookDetailFragment, bundle)
        }
        recyclerView.adapter = bookAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSearchView() {
        searchView.isIconified = false

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    performSearch(query)
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Opsional: Kalau mau "Live Search" (langsung muncul pas ngetik)
                // Aktifkan baris di bawah ini:
                // performSearch(newText)
                return false
            }
        })
    }

    private fun performSearch(query: String?) {
        val searchText = query?.lowercase() ?: ""

        if (searchText.isEmpty()) {
            bookAdapter.updateData(emptyList())
            return
        }

        // --- FILTERING LOKAL (Cepat & Efisien) ---
        // Cari buku yang judul ATAU penulisnya mengandung text pencarian
        val filteredList = fullBookList.filter { book ->
            book.title.lowercase().contains(searchText) ||
                    book.author.lowercase().contains(searchText)
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "Buku tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

        bookAdapter.updateData(filteredList)
    }

    private fun showLoading(isLoading: Boolean) {
        // Karena lokal sangat cepat, loading bar mungkin cuma kedip sebentar
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}