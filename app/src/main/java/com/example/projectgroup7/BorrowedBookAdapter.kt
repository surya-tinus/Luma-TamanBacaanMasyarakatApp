package com.example.projectgroup7.network

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.projectgroup7.databinding.ItemBorrowedBookBinding
import com.example.projectgroup7.model.BorrowedBook
import java.util.concurrent.TimeUnit

class BorrowedBookAdapter(private val books: List<BorrowedBook>) :
    RecyclerView.Adapter<BorrowedBookAdapter.VH>() {

    inner class VH(val binding: ItemBorrowedBookBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemBorrowedBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val book = books[position]
        with(holder.binding) {
            tvBookTitle.text = book.title
            tvBookAuthor.text = "By ${book.author}"
            tvBorrowDate.text = "Borrowed: ${book.borrowDate}"

            if (book.status == "returned") {
                tvRemainingTime.text = "Returned on ${book.returnDate ?: "—"}"
                tvRemainingTime.setTextColor(ContextCompat.getColor(root.context, android.R.color.darker_gray))
            } else {
                val days = TimeUnit.MILLISECONDS.toDays(book.remainingTime)
                val hours = TimeUnit.MILLISECONDS.toHours(book.remainingTime) % 24
                val remainingText = when {
                    book.remainingTime <= 0L -> "Due today"
                    days > 0 -> "Remaining: $days day${if (days > 1) "s" else ""}"
                    else -> "Remaining: $hours hour${if (hours > 1) "s" else ""}"
                }
                tvRemainingTime.text = remainingText
                // color: red if <=1 day, teal otherwise
                tvRemainingTime.setTextColor(
                    if (days <= 1) Color.RED
                    else Color.parseColor("#009688")
                )
            }
        }
    }

    override fun getItemCount(): Int = books.size
}
