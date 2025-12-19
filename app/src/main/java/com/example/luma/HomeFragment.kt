package com.example.luma

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView // Jangan lupa import ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.luma.database.Book
import com.example.luma.database.viewmodels.AnnouncementViewModel
import com.example.luma.database.viewmodels.BookViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var rvCategory: RecyclerView
    private lateinit var rvNewest: RecyclerView
    private lateinit var categoryAdapter: BookAdapter
    private lateinit var newestAdapter: BookAdapter
    private lateinit var announcementAdapter: AnnouncementAdapter

    private lateinit var bookViewModel: BookViewModel
    private lateinit var announcementViewModel: AnnouncementViewModel

    private var allBooksList = listOf<Book>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGreeting(view) // Update greeting + gambar
        setupViewModels()
        setupAnnouncements(view)
        setupRecyclerViews(view)
        setupObservers(view)
        setupClickListeners(view)
        setupDynamicCategoryChips(view)
    }

    // 1. SETUP GREETING & DYNAMIC IMAGE
    private fun setupGreeting(view: View) {
        val greetingTextView = view.findViewById<TextView>(R.id.tv_greeting)
        val usernameGreeting = view.findViewById<TextView>(R.id.username_greeting)
        val headerImage = view.findViewById<ImageView>(R.id.iv_header_illustration) // ID Baru

        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "User123456")

        // Ambil Jam Sekarang
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        // Tentukan Teks & Gambar berdasarkan jam
        // Pastikan nama file gambar di drawable sesuai:
        // greetings_pagi, greetings_siang, greetings_sore, greetings_malam
        val (greetingText, imageResId) = when (currentHour) {
            in 5..10 -> "Selamat Pagi," to R.drawable.greetings_pagi
            in 11..14 -> "Selamat Siang," to R.drawable.greetings_siang
            in 15..17 -> "Selamat Sore," to R.drawable.greetings_sore
            else -> "Selamat Malam," to R.drawable.greetings_malam
        }

        greetingTextView.text = greetingText
        usernameGreeting.text = username

        // Update Gambar Header
        try {
            headerImage.setImageResource(imageResId)
        } catch (e: Exception) {
            // Fallback kalau gambar belum ada, pakai default library
            headerImage.setImageResource(R.drawable.library)
        }
    }

    // 2. SETUP VIEW MODELS
    private fun setupViewModels() {
        bookViewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]
        announcementViewModel = ViewModelProvider(requireActivity())[AnnouncementViewModel::class.java]
    }

    // 3. SETUP PENGUMUMAN (SLIDER)
    private fun setupAnnouncements(view: View) {
        val vpAnnouncements = view.findViewById<ViewPager2>(R.id.vpAnnouncements)
        val tabIndicator = view.findViewById<TabLayout>(R.id.tabIndicator)

        announcementAdapter = AnnouncementAdapter(emptyList(), isAdmin = false) { }
        vpAnnouncements.adapter = announcementAdapter

        TabLayoutMediator(tabIndicator, vpAnnouncements) { _, _ -> }.attach()
    }

    // 4. SETUP RECYCLERVIEWS
    private fun setupRecyclerViews(view: View) {
        // A. Kategori (Horizontal)
        rvCategory = view.findViewById(R.id.rv_category_books)
        rvCategory.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        categoryAdapter = BookAdapter(emptyList()) { openDetail(it) }
        rvCategory.adapter = categoryAdapter

        // B. Buku Terbaru (Horizontal)
        rvNewest = view.findViewById(R.id.rv_new_books)
        rvNewest.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        newestAdapter = BookAdapter(emptyList()) { openDetail(it) }
        rvNewest.adapter = newestAdapter
    }

    // 5. OBSERVERS & CHIP LOGIC
    private fun setupObservers(view: View) {
        // Observer Buku
        bookViewModel.allBooks.observe(viewLifecycleOwner) { books ->
            allBooksList = books
            newestAdapter.updateData(books.reversed().take(5))

            // Default: Tampilkan semua (pakai UI Helper)
            updateCategoryUI(books)
        }

        // Observer Pengumuman
        announcementViewModel.announcements.observe(viewLifecycleOwner) { list ->
            val cvAnnouncement = view.findViewById<View>(R.id.cvAnnouncement)
            if (list.isNotEmpty()) {
                cvAnnouncement.visibility = View.VISIBLE
                announcementAdapter.updateData(list)
            } else {
                cvAnnouncement.visibility = View.GONE
            }
        }

        // Listener Chip Group (Filter Kategori)
        val chipGroup = view.findViewById<ChipGroup>(R.id.layout_categories)
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds.first())
                val categoryName = chip?.text.toString()

                if (categoryName.equals("Semua", ignoreCase = true)) {
                    updateCategoryUI(allBooksList) // Panggil helper UI
                } else {
                    filterCategory(categoryName)
                }
            } else {
                updateCategoryUI(allBooksList) // Panggil helper UI
            }
        }


    }

    // 6. SETUP TOMBOL "LIHAT SEMUA"
    private fun setupClickListeners(view: View) {
        val navAction = R.id.action_homeFragment_to_exploreFragment

        view.findViewById<TextView>(R.id.btn_see_all_category).setOnClickListener {
            try { requireView().findNavController().navigate(navAction) }
            catch (e: Exception) { }
        }

        view.findViewById<TextView>(R.id.btn_see_all_new).setOnClickListener {
            try { requireView().findNavController().navigate(navAction) }
            catch (e: Exception) { }
        }
    }

    // 7. CHIP

    private fun setupDynamicCategoryChips(view: View) {
        val chipGroup = view.findViewById<ChipGroup>(R.id.layout_categories)

        // 1. Tambahkan Chip "Semua" terlebih dahulu secara manual
        addChipToGroup(chipGroup, "Semua", isChecked = true)

        // 2. Ambil data kategori dari Firestore
        db.collection("categories")
            .orderBy("name") // Urutkan berdasarkan nama
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val categoryName = document.getString("name")
                    if (categoryName != null) {
                        addChipToGroup(chipGroup, categoryName, isChecked = false)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal memuat kategori", Toast.LENGTH_SHORT).show()
            }

        // 3. Listener (Dipindahkan ke sini atau disesuaikan dengan logic addChip)
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds.first())
                val categoryName = chip?.text.toString()

                if (categoryName.equals("Semua", ignoreCase = true)) {
                    updateCategoryUI(allBooksList)
                } else {
                    filterCategory(categoryName)
                }
            } else {
                // Opsional: Kalau di-uncheck semua, balik ke Semua
                // Tapi karena 'selectionRequired=true', kondisi ini mungkin jarang terjadi
                updateCategoryUI(allBooksList)
            }
        }
    }

    private fun addChipToGroup(chipGroup: ChipGroup, categoryName: String, isChecked: Boolean) {
        val chip = Chip(context)
        chip.text = categoryName
        chip.isCheckable = true
        chip.isChecked = isChecked

        // Style Chip agar sesuai desain (Perlu menyesuaikan dengan style/color resource kamu)
        // Cara manual set style programmatically agak tricky, paling mudah set propertinya langsung:
        chip.setChipBackgroundColorResource(R.color.selector_chip_background) // Pastikan file selector ini ada
        chip.setTextColor(resources.getColorStateList(R.color.selector_chip_text, null)) // Pastikan file selector ini ada

        // Atur agar behave seperti radio button di dalam group
        chip.id = View.generateViewId()

        chipGroup.addView(chip)
    }

    // --- HELPER FUNCTIONS ---

    // Fungsi Helper untuk Update UI Kategori (Handle Empty State)
    private fun updateCategoryUI(list: List<Book>) {
        val rvCategory = requireView().findViewById<RecyclerView>(R.id.rv_category_books)
        val tvEmpty = requireView().findViewById<TextView>(R.id.tv_empty_category)

        if (list.isEmpty()) {
            rvCategory.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            rvCategory.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            categoryAdapter.updateData(list)
        }
    }

    private fun filterCategory(category: String) {
        val filteredList = allBooksList.filter { book ->
            book.category.contains(category, ignoreCase = true)
        }
        updateCategoryUI(filteredList) // Panggil helper UI
    }

    private fun openDetail(selectedBook: Book) {
        val bundle = Bundle().apply {
            putParcelable("selectedBook", selectedBook)
        }
        try {
            requireView().findNavController()
                .navigate(R.id.action_homeFragment_to_bookDetailFragment, bundle)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error Navigasi", Toast.LENGTH_SHORT).show()
        }
    }
}