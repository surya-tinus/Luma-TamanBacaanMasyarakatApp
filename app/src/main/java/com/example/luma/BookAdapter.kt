package com.example.luma

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.luma.database.Book

class BookAdapter(
    private var bookList: List<Book>, // Ubah jadi 'var' supaya bisa di-update
    private val onItemClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_book_cover)
        val titleView: TextView = itemView.findViewById(R.id.tv_book_title)
        val categoryView: TextView = itemView.findViewById(R.id.tv_book_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]

        holder.titleView.text = book.title
        holder.categoryView.text = book.category

        // Cek apakah imagePath kosong? (Data dummy stok awal)
        if (book.imagePath.isNullOrEmpty()) {
            // Tampilkan gambar default (Logo)
            Glide.with(holder.itemView.context)
                .load(R.drawable.book_placeholder) // Pastikan gambar ini ada di drawable
                .into(holder.imageView)
        } else {
            // Jika nanti Admin upload foto, tampilkan dari path/URI-nya
            Glide.with(holder.itemView.context)
                .load(book.imagePath)
                .into(holder.imageView)
        }

        holder.itemView.setOnClickListener {
            onItemClick(book)
        }
    }

    override fun getItemCount() = bookList.size

    // --- FUNGSI PENTING ---
    // Fungsi ini dipanggil Activity saat ada data baru dari Database
    fun updateData(newBooks: List<Book>) {
        bookList = newBooks
        notifyDataSetChanged() // Refresh tampilan list
    }
}