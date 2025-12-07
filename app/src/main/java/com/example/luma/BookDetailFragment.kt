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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 1. AMBIL DATA DARI ARGUMENT ---
        @Suppress("DEPRECATION")
        currentBook = arguments?.getParcelable("selectedBook") ?: run {
            findNavController().navigateUp()
            return
        }

        // --- 2. INIT VIEWMODEL ---
        bookViewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]

        // --- 3. HUBUNGKAN VIEW (FIXED ID) ---
        val ivCover = view.findViewById<ImageView>(R.id.iv_book_cover)
        val tvTitle = view.findViewById<TextView>(R.id.tv_book_title)
        val tvAuthor = view.findViewById<TextView>(R.id.tv_book_author)
        val tvSynopsis = view.findViewById<TextView>(R.id.tv_book_description)

        val chipCategory = view.findViewById<TextView>(R.id.tv_book_category)
        val chipStock = view.findViewById<TextView>(R.id.tv_book_stock)

        // PERBAIKAN DI SINI:
        // Ganti R.id.rv_reviews menjadi R.id.tv_rating_value (sesuai XML kamu)
        val tvRatingValue = view.findViewById<TextView>(R.id.tv_rating_value)
        val ratingBar = view.findViewById<android.widget.RatingBar>(R.id.rating_bar)

        val btnBack = view.findViewById<View>(R.id.btn_back)
        val btnBorrow = view.findViewById<Button>(R.id.btn_borrow)

        // --- 4. ISI DATA KE TAMPILAN ---
        tvTitle.text = currentBook.title
        tvAuthor.text = currentBook.author
        tvSynopsis.text = currentBook.synopsis
        chipCategory.text = currentBook.category
        chipStock.text = "Stock: ${currentBook.stock}"

        // Set Rating (Text dan Bintang)
        tvRatingValue.text = "${currentBook.rating}"
        ratingBar.rating = currentBook.rating.toFloat()

        if (currentBook.imagePath.isNullOrEmpty()) {
            Glide.with(this).load(R.drawable.logo_alt).into(ivCover)
        } else {
            Glide.with(this).load(currentBook.imagePath).into(ivCover)
        }

        val rvReviews = view.findViewById<RecyclerView>(R.id.rv_reviews)
        val tvNoReviews = view.findViewById<TextView>(R.id.tv_no_reviews) // <-- ID Baru

        if (rvReviews != null) {
            rvReviews.layoutManager = LinearLayoutManager(context)
            val reviewAdapter = ReviewAdapter(emptyList())
            rvReviews.adapter = reviewAdapter

            bookViewModel.fetchBookReviews(currentBook.id)

            bookViewModel.bookReviews.observe(viewLifecycleOwner) { reviews ->
                if (reviews.isNotEmpty()) {
                    // ADA REVIEW
                    rvReviews.visibility = View.VISIBLE
                    tvNoReviews.visibility = View.GONE
                    reviewAdapter.updateData(reviews)
                } else {
                    // KOSONG
                    rvReviews.visibility = View.GONE
                    tvNoReviews.visibility = View.VISIBLE
                }
            }
        }

        // --- 6. LOGIKA TOMBOL ---
        btnBorrow.setOnClickListener {
            if (currentBook.stock <= 0) {
                Toast.makeText(requireContext(), "Yah, stok buku habis!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val username = sharedPref.getString("username", null)

            if (username != null) {
                bookViewModel.borrowBook(currentBook, username)
            } else {
                Toast.makeText(requireContext(), "Silakan login ulang!", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // --- 7. OBSERVASI STATUS PINJAM ---
        bookViewModel.borrowStatus.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

                if (message.contains("Berhasil")) {
                    btnBorrow.isEnabled = false
                    btnBorrow.text = "Buku Dipinjam"
                    chipStock.text = "Stock: ${currentBook.stock - 1}"
                }
                bookViewModel.resetBorrowStatus()
            }
        }
    }
}