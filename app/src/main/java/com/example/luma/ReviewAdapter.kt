package com.example.luma

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.luma.database.Loan

class ReviewAdapter(private var reviews: List<Loan>) :
    RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvReviewerName)
        val tvRating: TextView = view.findViewById(R.id.tvReviewRating)
        val tvContent: TextView = view.findViewById(R.id.tvReviewContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = reviews[position]

        // Ambil nama dari field 'reviewerName' (yang kita simpan tadi), atau default userId
        // Karena di Loan.kt belum ada field reviewerName, kita bisa akses manual via map atau update Loan.kt
        // Biar gampang, kita update Loan.kt sedikit di langkah berikutnya ya.
        holder.tvName.text = item.reviewerName.ifEmpty { "Anonymous" }
        holder.tvName.text = "User Review" // Nanti diganti
        holder.tvRating.text = "⭐ ${item.userRating}"
        holder.tvContent.text = item.userReview
    }

    override fun getItemCount() = reviews.size

    fun updateData(newReviews: List<Loan>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}