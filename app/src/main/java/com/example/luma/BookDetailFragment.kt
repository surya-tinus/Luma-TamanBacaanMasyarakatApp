package com.example.luma

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.luma.database.Book
import com.example.luma.database.viewmodels.BookViewModel

class BookDetailFragment : Fragment() {

    private lateinit var currentBook: Book
    private lateinit var bookViewModel: BookViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_book_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // =============================
        // 1. AMBIL DATA BUKU
        // =============================
        @Suppress("DEPRECATION")
        currentBook = arguments?.getParcelable("selectedBook") ?: run {
            findNavController().navigateUp()
            return
        }

        // =============================
        // 2. INIT VIEWMODEL
        // =============================
        bookViewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]

        // =============================
        // 3. HUBUNGKAN VIEW
        // =============================
        val ivCover = view.findViewById<ImageView>(R.id.iv_book_cover)
        val tvTitle = view.findViewById<TextView>(R.id.tv_book_title)
        val tvAuthor = view.findViewById<TextView>(R.id.tv_book_author)
        val tvSynopsis = view.findViewById<TextView>(R.id.tv_book_description)

        val chipCategory = view.findViewById<TextView>(R.id.tv_book_category)
        val chipStock = view.findViewById<TextView>(R.id.tv_book_stock)

        val tvRatingValue = view.findViewById<TextView>(R.id.tv_rating_value)
        val ratingBar = view.findViewById<android.widget.RatingBar>(R.id.rating_bar)

        val btnBorrow = view.findViewById<Button>(R.id.btn_borrow)
        val btnBack = view.findViewById<View>(R.id.btn_back)

        // =============================
        // 4. ISI DATA KE UI
        // =============================
        tvTitle.text = currentBook.title
        tvAuthor.text = currentBook.author
        tvSynopsis.text = currentBook.synopsis
        chipCategory.text = currentBook.category
        chipStock.text = "Stock: ${currentBook.stock}"

        tvRatingValue.text = currentBook.rating.toString()
        ratingBar.rating = currentBook.rating.toFloat()

        if (currentBook.imagePath.isNullOrEmpty()) {
            Glide.with(this).load(R.drawable.logo_alt).into(ivCover)
        } else {
            Glide.with(this).load(currentBook.imagePath).into(ivCover)
        }

        // =============================
        // 5. LOGIKA AWAL TOMBOL PINJAM
        // =============================
        if (currentBook.stock <= 0) {
            btnBorrow.isEnabled = false
            btnBorrow.isClickable = false
            btnBorrow.text = "Out of Stock"
            btnBorrow.alpha = 0.5f
        } else {
            btnBorrow.isEnabled = true
            btnBorrow.isClickable = true
            btnBorrow.text = "Pinjam"
            btnBorrow.alpha = 1f
        }

        // =============================
        // 6. REVIEW SECTION
        // =============================
        val rvReviews = view.findViewById<RecyclerView>(R.id.rv_reviews)
        val tvNoReviews = view.findViewById<TextView>(R.id.tv_no_reviews)

        rvReviews.layoutManager = LinearLayoutManager(requireContext())
        val reviewAdapter = ReviewAdapter(emptyList())
        rvReviews.adapter = reviewAdapter

        bookViewModel.fetchBookReviews(currentBook.id)
        bookViewModel.bookReviews.observe(viewLifecycleOwner) { reviews ->
            if (reviews.isNotEmpty()) {
                rvReviews.visibility = View.VISIBLE
                tvNoReviews.visibility = View.GONE
                reviewAdapter.updateData(reviews)
            } else {
                rvReviews.visibility = View.GONE
                tvNoReviews.visibility = View.VISIBLE
            }
        }

        // =============================
// 7. CLICK PINJAM (DOUBLE SAFETY + NOTIFICATION)
// =============================
        btnBorrow.setOnClickListener {
            // Cek Stok
            if (currentBook.stock <= 0) {
                Toast.makeText(requireContext(), "Yah, stok buku habis!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cek User Login
            val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val username = sharedPref.getString("username", null)

            if (username == null) {
                Toast.makeText(requireContext(), "Silakan login ulang!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ========================================================
            // FITUR NATIVE: LOCAL NOTIFICATION (ALARM MANAGER)
            // ========================================================
            // Skenario: Kita set pengingat 5 detik dari sekarang untuk demo.
            // Nanti bisa diganti 7 hari (masa pinjam) untuk production.
            val triggerTime = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.SECOND, 5)
            }.timeInMillis

            // Gunakan ID unik untuk tiap buku (misal hashCode dari judul)
            // supaya notifikasi buku A tidak menimpa notifikasi buku B
            val uniqueId = currentBook.title.hashCode()

            com.example.luma.utils.AlarmScheduler.scheduleNotification(
                context = requireContext(),
                timeInMillis = triggerTime,
                title = "Pengingat Pengembalian",
                message = "Jangan lupa kembalikan buku '${currentBook.title}' ya!",
                reqCode = uniqueId
            )
            // ========================================================

            // Lanjut proses pinjam ke database
            bookViewModel.borrowBook(currentBook, username)
        }

        // =============================
        // 8. TOMBOL KEMBALI
        // =============================
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // =============================
        // 9. OBSERVASI HASIL PINJAM
        // =============================
        bookViewModel.borrowStatus.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

                if (message.contains("Berhasil")) {
                    currentBook = currentBook.copy(stock = currentBook.stock - 1)

                    chipStock.text = "Stock: ${currentBook.stock}"
                    btnBorrow.isEnabled = false
                    btnBorrow.isClickable = false
                    btnBorrow.text = "Buku Dipinjam"
                    btnBorrow.alpha = 0.5f
                }

                bookViewModel.resetBorrowStatus()
            }
        }
    }
}
