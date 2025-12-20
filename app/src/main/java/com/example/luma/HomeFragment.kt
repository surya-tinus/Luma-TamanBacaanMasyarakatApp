package com.example.luma

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.example.luma.api.RecommendationResponse
import com.example.luma.api.RetrofitClient
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class HomeFragment : Fragment() {

    // Views Existing
    private lateinit var rvCategory: RecyclerView
    private lateinit var rvNewest: RecyclerView
    private lateinit var categoryAdapter: BookAdapter
    private lateinit var newestAdapter: BookAdapter
    private lateinit var announcementAdapter: AnnouncementAdapter

    // Views Baru untuk Rekomendasi
    private lateinit var rvRecommendations: RecyclerView
    private lateinit var recommendationAdapter: BookAdapter
    private lateinit var layoutRecommendationSection: LinearLayout

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

        setupGreeting(view)
        setupViewModels()
        setupAnnouncements(view)
        setupRecyclerViews(view)
        setupObservers(view)
        setupClickListeners(view)
        setupDynamicCategoryChips(view)

        // PANGGIL LOGIC REKOMENDASI AI
        fetchRecommendations()
    }

    // 1. SETUP GREETING
    private fun setupGreeting(view: View) {
        val greetingTextView = view.findViewById<TextView>(R.id.tv_greeting)
        val usernameGreeting = view.findViewById<TextView>(R.id.username_greeting)
        val headerImage = view.findViewById<ImageView>(R.id.iv_header_illustration)

        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "User")

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val (greetingText, imageResId) = when (currentHour) {
            in 5..10 -> "Selamat Pagi," to R.drawable.greetings_pagi
            in 11..14 -> "Selamat Siang," to R.drawable.greetings_siang
            in 15..17 -> "Selamat Sore," to R.drawable.greetings_sore
            else -> "Selamat Malam," to R.drawable.greetings_malam
        }

        greetingTextView.text = greetingText
        usernameGreeting.text = username

        try {
            headerImage.setImageResource(imageResId)
        } catch (e: Exception) {
            headerImage.setImageResource(R.drawable.library)
        }
    }

    // 2. SETUP VIEW MODELS
    private fun setupViewModels() {
        bookViewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]
        announcementViewModel = ViewModelProvider(requireActivity())[AnnouncementViewModel::class.java]
    }

    // 3. SETUP PENGUMUMAN
    private fun setupAnnouncements(view: View) {
        val vpAnnouncements = view.findViewById<ViewPager2>(R.id.vpAnnouncements)
        val tabIndicator = view.findViewById<TabLayout>(R.id.tabIndicator)

        announcementAdapter = AnnouncementAdapter(emptyList(), isAdmin = false) { }
        vpAnnouncements.adapter = announcementAdapter

        TabLayoutMediator(tabIndicator, vpAnnouncements) { _, _ -> }.attach()
    }

    // 4. SETUP RECYCLERVIEWS (Kategori, Terbaru, Rekomendasi)
    private fun setupRecyclerViews(view: View) {
        // A. Kategori
        rvCategory = view.findViewById(R.id.rv_category_books)
        rvCategory.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        categoryAdapter = BookAdapter(emptyList()) { openDetail(it) }
        rvCategory.adapter = categoryAdapter

        // B. Terbaru
        rvNewest = view.findViewById(R.id.rv_new_books)
        rvNewest.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        newestAdapter = BookAdapter(emptyList()) { openDetail(it) }
        rvNewest.adapter = newestAdapter

        // C. Rekomendasi AI
        layoutRecommendationSection = view.findViewById(R.id.layout_recommendation_section)
        rvRecommendations = view.findViewById(R.id.rv_recommendations)
        rvRecommendations.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recommendationAdapter = BookAdapter(emptyList()) { openDetail(it) }
        rvRecommendations.adapter = recommendationAdapter
    }

    // 5. OBSERVERS
    private fun setupObservers(view: View) {
        bookViewModel.allBooks.observe(viewLifecycleOwner) { books ->
            allBooksList = books
            newestAdapter.updateData(books.reversed().take(5))
            updateCategoryUI(books)
        }

        announcementViewModel.announcements.observe(viewLifecycleOwner) { list ->
            val cvAnnouncement = view.findViewById<View>(R.id.cvAnnouncement)
            if (list.isNotEmpty()) {
                cvAnnouncement.visibility = View.VISIBLE
                announcementAdapter.updateData(list)
            } else {
                cvAnnouncement.visibility = View.GONE
            }
        }
    }

    // 6. CHIPS KATEGORI DINAMIS
    private fun setupDynamicCategoryChips(view: View) {
        val chipGroup = view.findViewById<ChipGroup>(R.id.layout_categories)

        addChipToGroup(chipGroup, "Semua", isChecked = true)

        db.collection("categories")
            .orderBy("name")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val categoryName = document.getString("name")
                    if (categoryName != null) {
                        addChipToGroup(chipGroup, categoryName, isChecked = false)
                    }
                }
            }

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
                updateCategoryUI(allBooksList)
            }
        }
    }

    private fun addChipToGroup(chipGroup: ChipGroup, categoryName: String, isChecked: Boolean) {
        val chip = Chip(context)
        chip.text = categoryName
        chip.isCheckable = true
        chip.isChecked = isChecked
        chip.setChipBackgroundColorResource(R.color.selector_chip_background)
        chip.setTextColor(resources.getColorStateList(R.color.selector_chip_text, null))
        chip.id = View.generateViewId()
        chipGroup.addView(chip)
    }

    // 7. CLICK LISTENERS (Lihat Semua)
    private fun setupClickListeners(view: View) {
        val navAction = R.id.action_homeFragment_to_exploreFragment
        view.findViewById<TextView>(R.id.btn_see_all_category).setOnClickListener {
            try { requireView().findNavController().navigate(navAction) } catch (e: Exception) { }
        }
        view.findViewById<TextView>(R.id.btn_see_all_new).setOnClickListener {
            try { requireView().findNavController().navigate(navAction) } catch (e: Exception) { }
        }
    }

    // =========================================================
    // LOGIKA REKOMENDASI AI
    // =========================================================
    private fun fetchRecommendations() {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        // Ambil UserID (Pastikan saat login kamu simpan key "userId")
        val userId = sharedPref.getString("userId", null)

        if (userId == null) {
            layoutRecommendationSection.visibility = View.GONE
            return
        }

        // 1. Panggil API Python
        RetrofitClient.instance.getRecommendations(userId).enqueue(object : Callback<RecommendationResponse> {
            override fun onResponse(call: Call<RecommendationResponse>, response: Response<RecommendationResponse>) {
                if (response.isSuccessful) {
                    val recommendedItems = response.body()?.recommendations
                    if (!recommendedItems.isNullOrEmpty()) {
                        // Dapat ID Buku dari API -> Ambil detailnya dari Firestore
                        val bookIds = recommendedItems.map { it.bookId }
                        fetchBooksFromFirestore(bookIds)
                    } else {
                        layoutRecommendationSection.visibility = View.GONE
                    }
                } else {
                    layoutRecommendationSection.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<RecommendationResponse>, t: Throwable) {
                // Gagal konek API (Server mati/Internet mati), sembunyikan section
                layoutRecommendationSection.visibility = View.GONE
                Log.e("API_ERROR", "Gagal load rekomendasi: ${t.message}")
            }
        })
    }

    private fun fetchBooksFromFirestore(bookIds: List<String>) {
        val recommendedBooks = mutableListOf<Book>()
        var processedCount = 0

        for (id in bookIds) {
            db.collection("books").document(id).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val book = document.toObject(Book::class.java)
                        if (book != null) {
                            book.id = document.id
                            recommendedBooks.add(book)
                        }
                    }
                    processedCount++
                    checkIfComplete(processedCount, bookIds.size, recommendedBooks)
                }
                .addOnFailureListener {
                    processedCount++
                    checkIfComplete(processedCount, bookIds.size, recommendedBooks)
                }
        }
    }

    private fun checkIfComplete(current: Int, total: Int, books: List<Book>) {
        if (current == total) {
            if (books.isNotEmpty()) {
                layoutRecommendationSection.visibility = View.VISIBLE
                recommendationAdapter.updateData(books)
            } else {
                layoutRecommendationSection.visibility = View.GONE
            }
        }
    }
    // =========================================================

    // HELPER FUNCTIONS
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
        updateCategoryUI(filteredList)
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