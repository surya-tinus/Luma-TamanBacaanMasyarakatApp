package com.example.luma

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.luma.database.Announcement
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AnnouncementDetailFragment : Fragment() {

    private lateinit var announcement: Announcement
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_announcement_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        announcement = requireArguments().getParcelable("announcement")!!

        val tvTitle = view.findViewById<TextView>(R.id.tvDetailTitle)
        val tvDate = view.findViewById<TextView>(R.id.tvDetailDate)
        val tvContent = view.findViewById<TextView>(R.id.tvDetailContent)
        val btnEdit = view.findViewById<Button>(R.id.btnEdit)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)

        tvTitle.text = announcement.title
        tvContent.text = announcement.content

        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        tvDate.text = sdf.format(announcement.date)

        btnEdit.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("announcement", announcement)
            }
            findNavController().navigate(R.id.action_announcementDetail_to_editAnnouncement, bundle)
        }

        btnDelete.setOnClickListener {
            confirmDelete()
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Announcement")
            .setMessage("Are you sure you want to delete this announcement?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("announcements")
                    .document(announcement.id)
                    .delete()
                    .addOnSuccessListener {
                        findNavController().popBackStack()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}