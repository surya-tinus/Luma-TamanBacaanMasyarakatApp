package com.example.luma

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.luma.database.Book // Pakai Book dari Database
import com.example.luma.database.viewmodels.BookViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var rvCategory: RecyclerView
    private lateinit var rvNewest: RecyclerView
    private lateinit var categoryAdapter: BookAdapter
    private lateinit var newestAdapter: BookAdapter

    // Kita pakai ViewModel, bukan Retrofit lagi
    private lateinit var bookViewModel: BookViewModel

    // Data List
    private var allBooksList = listOf<Book>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false) // Layout tetap sama!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup Greeting & Username (Sama seperti kodemu)
        val greetingTextView = view.findViewById<TextView>(R.id.tv_greeting)
        val usernameGreeting = view.findViewById<TextView>(R.id.username_greeting)
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Ambil nama user yang login (Default: User123456)
        val username = sharedPref.getString("username", "User123456")
        greetingTextView.text = getGreeting()
        usernameGreeting.text = username

        // 2. Inisialisasi ViewModel
        bookViewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]

        // 3. Setup RecyclerView Category (Horizontal)
        rvCategory = view.findViewById(R.id.rv_category_books)
        rvCategory.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val categorySnap = LinearSnapHelper()
        categorySnap.attachToRecyclerView(rvCategory)

        // Inisialisasi Adapter dengan list kosong dulu
        categoryAdapter = BookAdapter(emptyList()) { selectedBook ->
            openDetail(selectedBook)
        }
        rvCategory.adapter = categoryAdapter

        // 4. Setup RecyclerView Newest (Grid)
        rvNewest = view.findViewById(R.id.rv_new_books)
        // Matikan nested scrolling biar smooth di dalam ScrollView
        rvNewest.isNestedScrollingEnabled = false
        rvNewest.layoutManager = GridLayoutManager(requireContext(), 3)

        newestAdapter = BookAdapter(emptyList()) { selectedBook ->
            openDetail(selectedBook)
        }
        rvNewest.adapter = newestAdapter

        // 5. Observasi Data dari Database (Room)
        // Begitu ada data (dari Seeding tadi), kode ini jalan otomatis
        bookViewModel.allBooks.observe(viewLifecycleOwner) { books ->
            allBooksList = books

            // Tampilkan semua buku di bagian "Newest"
            // (Logika: balik urutan biar yang baru di atas, ambil 6 teratas saja)
            newestAdapter.updateData(books.reversed().take(6))

            // Default kategori awal: Romance
            filterCategory("Romance")
        }

        // 6. ChipGroup Listener (Filter Kategori Lokal)
        val chipGroup = view.findViewById<ChipGroup>(R.id.layout_categories)
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds.first())
                chip?.let {
                    val categoryName = it.text.toString()
                    filterCategory(categoryName)
                }
            } else {
                // Kalau tidak ada yang dipilih, tampilkan semua di kategori
                categoryAdapter.updateData(allBooksList)
            }
        }

        // 7. Explore Button (Tetap navigasi ke fragment explore)
        view.findViewById<Button>(R.id.btn_explore).setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_homeFragment_to_exploreFragment)
        }

        bookViewModel.seedData()
    }

    // Fungsi Filter Kategori (Lokal)
    private fun filterCategory(category: String) {
        // Cari buku yang kategorinya mengandung teks tersebut (ignore case)
        val filteredList = allBooksList.filter { book ->
            book.category.contains(category, ignoreCase = true)
        }
        categoryAdapter.updateData(filteredList)
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

    // Fungsi Buka Detail (Navigasi)
    private fun openDetail(selectedBook: Book) {
        // 1. Bungkus data buku ke dalam Bundle
        val bundle = Bundle().apply {
            putParcelable("selectedBook", selectedBook)
        }

        // 2. Lakukan Navigasi (Pastikan ID action-nya benar sesuai nav_graph)
        // Cek apakah ID action kamu 'action_homeFragment_to_bookDetailFragment' ?
        try {
            requireView().findNavController()
                .navigate(R.id.action_homeFragment_to_bookDetailFragment, bundle)
        } catch (e: Exception) {
            // Kalau error (misal lupa bikin panah di nav_graph), muncul toast ini
            Toast.makeText(requireContext(), "Error Navigasi: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}