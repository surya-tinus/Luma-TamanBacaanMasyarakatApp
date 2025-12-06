package com.example.luma

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.luma.database.Announcement
import java.text.SimpleDateFormat
import java.util.Locale

class AnnouncementAdapter(private var list: List<Announcement>) :
    RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Hubungkan dengan ID di item_announcement.xml
        val tvDate: TextView = view.findViewById(R.id.tvAnnounceDate)
        val tvTitle: TextView = view.findViewById(R.id.tvAnnounceTitle)
        val tvContent: TextView = view.findViewById(R.id.tvAnnounceContent)
        val tvTime: TextView = view.findViewById(R.id.tvAnnounceTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // PENTING: Gunakan layout item_announcement yang baru
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_announcement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvTitle.text = item.title
        holder.tvContent.text = item.content

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        holder.tvDate.text = dateFormat.format(item.date)
        holder.tvTime.text = "Pukul " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(item.date)
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<Announcement>) {
        list = newList
        notifyDataSetChanged()
    }
}