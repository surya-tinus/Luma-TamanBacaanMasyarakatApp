package com.example.luma

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.luma.database.Loan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class LoanAdapter(
    private var loanList: List<Loan>,
    private val onItemClick: (Loan) -> Unit // Aksi kalau item diklik (misal buat balikin buku)
) : RecyclerView.Adapter<LoanAdapter.LoanViewHolder>() {

    // Hubungkan dengan ID yang ada di item_borrowed_book.xml kamu
    inner class LoanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvBookAuthor)
        val tvDate: TextView = itemView.findViewById(R.id.tvBorrowDate)
        val tvRemaining: TextView = itemView.findViewById(R.id.tvRemainingTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        // PENTING: Gunakan layout 'item_borrowed_book' punya kamu
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_borrowed_book, parent, false)
        return LoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
        val loan = loanList[position]

        // 1. Set Judul & Penulis
        holder.tvTitle.text = loan.bookTitle
        holder.tvAuthor.text = loan.bookAuthor

        // 2. Format Tanggal Pinjam (Contoh: 12 Oct 2025)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = loan.borrowDate?.let { dateFormat.format(it) } ?: "-"
        holder.tvDate.text = "Borrowed on: $dateString"

        // 3. Hitung Sisa Waktu (Remaining Days)
        if (loan.dueDate != null) {
            val today = Date()
            val diffInMillis = loan.dueDate.time - today.time
            // Konversi dari milidetik ke Hari
            val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffInMillis)

            if (daysRemaining > 0) {
                holder.tvRemaining.text = "Remaining: $daysRemaining days"
                holder.tvRemaining.setTextColor(Color.parseColor("#4CAF50")) // Hijau (Aman)
            } else if (daysRemaining == 0L) {
                holder.tvRemaining.text = "Due Today!"
                holder.tvRemaining.setTextColor(Color.parseColor("#FF9800")) // Oranye (Hati-hati)
            } else {
                val overdue = Math.abs(daysRemaining)
                holder.tvRemaining.text = "Overdue by $overdue days"
                holder.tvRemaining.setTextColor(Color.RED) // Merah (Telat)
            }
        } else {
            holder.tvRemaining.text = "-"
        }

        // Klik item (opsional, misal mau detail pengembalian)
        holder.itemView.setOnClickListener {
            onItemClick(loan)
        }
    }

    override fun getItemCount() = loanList.size

    fun updateData(newLoans: List<Loan>) {
        loanList = newLoans
        notifyDataSetChanged()
    }
}