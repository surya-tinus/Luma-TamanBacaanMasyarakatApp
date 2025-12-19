package com.example.luma

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView // Pastikan ImageView ada
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BookAdminAdapter(
    private val list: List<ManageBooksFragment.BookLocal>,
    private val onClick: (ManageBooksFragment.BookLocal) -> Unit,
    private val onEdit: (ManageBooksFragment.BookLocal) -> Unit,
    private val onDelete: (ManageBooksFragment.BookLocal) -> Unit
) : RecyclerView.Adapter<BookAdminAdapter.ViewHolder>() {

    // Perbaikan ada di class ViewHolder ini
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.iv_book_cover)
        val tvTitle: TextView = view.findViewById(R.id.tv_book_title)
        val tvCategory: TextView = view.findViewById(R.id.tv_book_category)

        // UBAH DARI Button MENJADI ImageView
        val btnEdit: ImageView = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_books_admin, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = list[position]
        holder.tvTitle.text = book.title
        holder.tvCategory.text = book.category

        // Menggunakan Glide untuk load gambar
        Glide.with(holder.itemView.context)
            .load(book.imageUrl)
            .placeholder(R.drawable.ic_launcher_background) // Pastikan drawable ini ada atau ganti icon lain
            .into(holder.ivCover)

        holder.itemView.setOnClickListener { onClick(book) }

        // Listener tetap sama, cuma tipe view-nya saja yang berubah
        holder.btnEdit.setOnClickListener { onEdit(book) }
        holder.btnDelete.setOnClickListener { onDelete(book) }
    }
}