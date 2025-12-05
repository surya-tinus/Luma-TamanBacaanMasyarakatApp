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
import com.bumptech.glide.Glide
import com.example.luma.database.Book
import com.example.luma.database.viewmodels.BookViewModel
import com.google.android.material.chip.Chip

class BookDetailFragment : Fragment() {

    // 1. Deklarasi Variabel untuk Buku yang sedang dilihat
    private lateinit var currentBook: Book

    // 2. Deklarasi ViewModel
    private lateinit var bookViewModel: BookViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Pastikan layout XML kamu namanya 'fragment_book_detail'
        return inflater.inflate(R.layout.fragment_book_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 3. AMBIL DATA BUKU DARI ARGUMENT (PENTING BIAR currentBook TIDAK MERAH)
        // "selectedBook" harus sama dengan nama key saat dikirim dari HomeFragment
        @Suppress("DEPRECATION")
        currentBook = arguments?.getParcelable("selectedBook") ?: run {
            // Kalau data ga ada, balik ke home biar ga crash
            findNavController().navigateUp()
            return
        }

        // 4. Inisialisasi ViewModel
        bookViewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]

        // 5. Hubungkan View (Pastikan ID ini ada di XML kamu)
        val ivCover = view.findViewById<ImageView>(R.id.iv_book_cover)
        val tvTitle = view.findViewById<TextView>(R.id.tv_book_title)
        val tvAuthor = view.findViewById<TextView>(R.id.tv_book_author)
        val tvSynopsis = view.findViewById<TextView>(R.id.tv_book_description)
        val chipCategory = view.findViewById<TextView>(R.id.tv_book_category)
        val chipStock = view.findViewById<TextView>(R.id.tv_book_stock)
        val btnBack = view.findViewById<View>(R.id.btn_back) // Bisa Button/ImageButton

        // Tombol Pinjam (Pastikan ID 'btn_borrow' sudah ada di XML)
        val btnBorrow = view.findViewById<Button>(R.id.btn_borrow)

        // 6. Tampilkan Data ke Layar
        tvTitle.text = currentBook.title
        tvAuthor.text = currentBook.author
        tvSynopsis.text = currentBook.synopsis
        chipCategory.text = currentBook.category
        chipStock.text = "Stok: ${currentBook.stock}"

        // Load Gambar
        if (currentBook.imagePath.isNullOrEmpty()) {
            Glide.with(this).load(R.drawable.logo_alt).into(ivCover)
        } else {
            Glide.with(this).load(currentBook.imagePath).into(ivCover)
        }

        // 7. AKSI TOMBOL PINJAM
        btnBorrow.setOnClickListener {
            // Cek Stok Dulu
            if (currentBook.stock <= 0) {
                Toast.makeText(requireContext(), "Yah, stok buku habis!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ambil Username user yang sedang login
            val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val username = sharedPref.getString("username", null)

            if (username != null) {
                // Panggil fungsi pinjam di ViewModel
                bookViewModel.borrowBook(currentBook, username)
            } else {
                Toast.makeText(requireContext(), "Silakan login ulang!", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol Back
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // 8. OBSERVASI HASIL PEMINJAMAN (Dari ViewModel)
        bookViewModel.borrowStatus.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

                // Jika sukses, tombol didisable biar gak spam klik
                if (message.contains("Berhasil")) {
                    btnBorrow.isEnabled = false
                    btnBorrow.text = "Buku Dipinjam"

                    // Update tampilan stok secara manual biar langsung keliatan berkurang
                    // (Walaupun nanti kalau di-refresh akan update otomatis dari firebase)
                    chipStock.text = "Stok: ${currentBook.stock - 1}"
                }

                bookViewModel.resetBorrowStatus()
            }
        }
    }
}