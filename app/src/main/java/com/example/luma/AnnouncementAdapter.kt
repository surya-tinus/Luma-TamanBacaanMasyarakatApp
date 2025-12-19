package com.example.luma

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.luma.database.Announcement
import java.text.SimpleDateFormat
import java.util.Locale

class AnnouncementAdapter(
    private var list: List<Announcement>,
    private val isAdmin: Boolean = false, // TAMBAHKAN PARAMETER INI (Default false buat user)
    private val onClick: (Announcement) -> Unit
) : RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvAnnounceDate)
        val tvTitle: TextView = view.findViewById(R.id.tvAnnounceTitle)
        val tvContent: TextView = view.findViewById(R.id.tvAnnounceContent)
        // Gunakan safe call (?.) karena tvTime mungkin didisable/gone di layout admin
        val tvTime: TextView? = view.findViewById(R.id.tvAnnounceTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // LOGIC PEMILIHAN LAYOUT
        val layoutId = if (isAdmin) {
            R.layout.item_announcement_admin // Layout baru yang wrap_content (Kecil)
        } else {
            R.layout.item_announcement // Layout lama yang match_parent (User Slider)
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvTitle.text = item.title
        holder.tvContent.text = item.content

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        holder.tvDate.text = dateFormat.format(item.date)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        // tvTime pakai safe call (?.) karena di layout admin mungkin tidak kita pasang
        holder.tvTime?.text = "Pukul ${timeFormat.format(item.date)}"

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    fun updateData(newList: List<Announcement>) {
        list = newList
        notifyDataSetChanged()
    }
}