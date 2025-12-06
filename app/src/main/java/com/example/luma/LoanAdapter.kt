package com.example.luma

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.luma.database.Loan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class LoanAdapter(
    private var loanList: List<Loan>,
    private val onItemClick: (Loan) -> Unit // Callback tunggal, logika dibedakan di Fragment
) : RecyclerView.Adapter<LoanAdapter.LoanViewHolder>() {

    inner class LoanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvBookAuthor)
        val tvDate: TextView = itemView.findViewById(R.id.tvBorrowDate)
        val tvRemaining: TextView = itemView.findViewById(R.id.tvRemainingTime)
        // Tambahkan definisi tombol yang baru dibuat di XML
        val btnAction: Button = itemView.findViewById(R.id.btnAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_borrowed_book, parent, false)
        return LoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
        val loan = loanList[position]

        // 1. Data Dasar
        holder.tvTitle.text = loan.bookTitle
        holder.tvAuthor.text = loan.bookAuthor

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = loan.borrowDate?.let { dateFormat.format(it) } ?: "-"
        holder.tvDate.text = "Borrowed on: $dateString"

        // 2. LOGIKA STATUS (Active vs History)
        if (loan.status == "active") {
            // --- MODE ON PROGRESS ---

            // A. Hitung Sisa Waktu
            if (loan.dueDate != null) {
                val today = Date()
                val diffInMillis = loan.dueDate.time - today.time
                val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffInMillis)

                if (daysRemaining > 0) {
                    holder.tvRemaining.text = "Remaining: $daysRemaining days"
                    holder.tvRemaining.setTextColor(Color.parseColor("#4CAF50")) // Hijau
                } else if (daysRemaining == 0L) {
                    holder.tvRemaining.text = "Due Today!"
                    holder.tvRemaining.setTextColor(Color.parseColor("#FF9800")) // Oranye
                } else {
                    val overdue = Math.abs(daysRemaining)
                    holder.tvRemaining.text = "Overdue by $overdue days"
                    holder.tvRemaining.setTextColor(Color.RED) // Merah
                }
            } else {
                holder.tvRemaining.text = "-"
            }

            // B. Tombol Return
            holder.btnAction.text = "Return Book"
            holder.btnAction.isEnabled = true
            holder.btnAction.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF9800")) // Oranye

            // Klik tombol untuk mengembalikan
            holder.btnAction.setOnClickListener {
                onItemClick(loan)
            }

        } else {
            // --- MODE HISTORY (RETURNED) ---

            holder.tvRemaining.text = "Status: Returned"
            holder.tvRemaining.setTextColor(Color.GRAY)

            // Cek apakah user sudah kasih rating?
            if (loan.userRating > 0) {
                // Sudah Review -> Tampilkan Bintang
                holder.btnAction.text = "★ ${loan.userRating} / 5.0"
                holder.btnAction.isEnabled = true // Tetap enable kalau mau edit review
                holder.btnAction.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50")) // Hijau
            } else {
                // Belum Review -> Tombol Rate
                holder.btnAction.text = "Rate & Review"
                holder.btnAction.isEnabled = true
                holder.btnAction.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#677e5d")) // Hijau Luma
            }

            // Klik tombol untuk memberi ulasan
            holder.btnAction.setOnClickListener {
                onItemClick(loan)
            }
        }
    }

    override fun getItemCount() = loanList.size

    fun updateData(newLoans: List<Loan>) {
        loanList = newLoans
        notifyDataSetChanged()
    }
}