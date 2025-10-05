package com.example.projectgroup7

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide

class BookDetailFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_detail, container, false)

        // Ambil semua data dari bundle
        val bookTitle = arguments?.getString("title")
        val bookCategory = arguments?.getString("category")
        val bookImageUrl = arguments?.getString("imageUrl")
        val bookDescription = arguments?.getString("description")

        // Hubungkan ke view (pastikan ID ini ada di layout Anda)
        val titleTextView = view.findViewById<TextView>(R.id.tv_book_title)
        val categoryTextView = view.findViewById<TextView>(R.id.tv_book_category)
        val descriptionTextView = view.findViewById<TextView>(R.id.tv_book_description)
        val imageView = view.findViewById<ImageView>(R.id.iv_book_cover)

        // Set data ke view
        titleTextView.text = bookTitle
        categoryTextView.text = bookCategory
        descriptionTextView.text = bookDescription

        // Gunakan Glide untuk memuat gambar dari URL
        Glide.with(this)
            .load(bookImageUrl)
            .into(imageView)

        // Tombol back
        val backButton = view.findViewById<ImageView>(R.id.btn_back)
        backButton.setOnClickListener {
            findNavController().navigateUp() // balik ke fragment sebelumnya
        }

        return view
    }
}
