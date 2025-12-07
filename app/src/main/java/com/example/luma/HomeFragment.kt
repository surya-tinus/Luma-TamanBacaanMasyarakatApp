package com.example.luma

import android.content.Context
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
import androidx.viewpager2.widget.ViewPager2 // Import Baru
import com.example.luma.database.Book
import com.example.luma.database.viewmodels.AnnouncementViewModel
import com.example.luma.database.viewmodels.BookViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout // Import Baru
import com.google.android.material.tabs.TabLayoutMediator // Import Baru
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var rvCategory: RecyclerView
    private lateinit var rvNewest: RecyclerView
    private lateinit var categoryAdapter: BookAdapter
    private lateinit var newestAdapter: BookAdapter

    // Adapter untuk Slider Pengumuman
    private lateinit var announcementAdapter: AnnouncementAdapter

    private lateinit var bookViewModel: BookViewModel
    private lateinit var announcementViewModel: AnnouncementViewModel

    private var allBooksList = listOf<Book>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Greeting
        val greetingTextView = view.findViewById<TextView>(R.id.tv_greeting)
        val usernameGreeting = view.findViewById<TextView>(R.id.username_greeting)
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "User123456")
        greetingTextView.text = getGreeting()
        usernameGreeting.text = username

        // Inisialisasi ViewModel
        bookViewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]
        announcementViewModel = ViewModelProvider(requireActivity())[AnnouncementViewModel::class.java]

        // --- SETUP CAROUSEL PENGUMUMAN (BARU) ---
        // Cari ID ViewPager2 dan TabLayout, BUKAN RecyclerView lagi
        val vpAnnouncements = view.findViewById<ViewPager2>(R.id.vpAnnouncements)
        val tabIndicator = view.findViewById<TabLayout>(R.id.tabIndicator)

        announcementAdapter = AnnouncementAdapter(emptyList())
        vpAnnouncements.adapter = announcementAdapter // Pasang adapter ke ViewPager

        // Hubungkan Titik-titik (Tab) dengan Slider
        TabLayoutMediator(tabIndicator, vpAnnouncements) { _, _ ->
            // Tidak ada judul tab, cuma titik
        }.attach()
        // ----------------------------------------

        // Setup RecyclerView Category
        rvCategory = view.findViewById(R.id.rv_category_books)
        rvCategory.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val categorySnap = LinearSnapHelper()
        categorySnap.attachToRecyclerView(rvCategory)
        categoryAdapter = BookAdapter(emptyList()) { selectedBook -> openDetail(selectedBook) }
        rvCategory.adapter = categoryAdapter

        // Setup RecyclerView Newest
        rvNewest = view.findViewById(R.id.rv_new_books)
        rvNewest.isNestedScrollingEnabled = false
        rvNewest.layoutManager = GridLayoutManager(requireContext(), 3)
        newestAdapter = BookAdapter(emptyList()) { selectedBook -> openDetail(selectedBook) }
        rvNewest.adapter = newestAdapter

        // --- OBSERVASI DATA ---

        // 1. Observasi Buku
        bookViewModel.allBooks.observe(viewLifecycleOwner) { books ->
            allBooksList = books
            newestAdapter.updateData(books.reversed().take(6))
            filterCategory("Romance")
        }

        // 2. Observasi Pengumuman
        announcementViewModel.announcements.observe(viewLifecycleOwner) { list ->
            // Cari CardView pembungkusnya
            val cvAnnouncement = view.findViewById<View>(R.id.cvAnnouncement)

            if (list.isNotEmpty()) {
                cvAnnouncement.visibility = View.VISIBLE
                announcementAdapter.updateData(list) // Update data slider
            } else {
                // Kalau mau disembunyikan total jika kosong:
                cvAnnouncement.visibility = View.GONE

                // Kalau mau tetap tampil tapi teks kosong (sesuai request kamu sebelumnya):
                // cvAnnouncement.visibility = View.VISIBLE
                // announcementAdapter.updateData(emptyList())
            }
        }

        // ChipGroup Listener
        val chipGroup = view.findViewById<ChipGroup>(R.id.layout_categories)
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds.first())
                chip?.let {
                    filterCategory(it.text.toString())
                }
            } else {
                categoryAdapter.updateData(allBooksList)
            }
        }

        // Explore Button
        view.findViewById<Button>(R.id.btn_explore).setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_homeFragment_to_exploreFragment)
        }
    }

    private fun filterCategory(category: String) {
        val filteredList = allBooksList.filter { book ->
            book.category.contains(category, ignoreCase = true)
        }
        categoryAdapter.updateData(filteredList)
    }

    private fun getGreeting(): String {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (currentHour) {
            in 5..11 -> "Selamat Pagi,"
            in 12..16 -> "Selamat Siang,"
            in 17..20 -> "Selamat Sore,"
            else -> "Selamat Malam,"
        }
    }

    private fun openDetail(selectedBook: Book) {
        val bundle = Bundle().apply {
            putParcelable("selectedBook", selectedBook)
        }
        try {
            requireView().findNavController()
                .navigate(R.id.action_homeFragment_to_bookDetailFragment, bundle)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error Navigasi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}