package com.example.luma

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.luma.database.Announcement
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AnnouncementDetailFragment : Fragment() {

    private lateinit var announcementId: String // Kita simpan ID-nya aja
    private lateinit var currentAnnouncement: Announcement // Data terbaru
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_announcement_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Ambil data awal dari arguments
        val initialData = requireArguments().getParcelable<Announcement>("announcement")!!
        announcementId = initialData.id
        currentAnnouncement = initialData

        val tvTitle = view.findViewById<TextView>(R.id.tvDetailTitle)
        val tvDate = view.findViewById<TextView>(R.id.tvDetailDate)
        val tvContent = view.findViewById<TextView>(R.id.tvDetailContent)
        val btnEdit = view.findViewById<Button>(R.id.btnEdit)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack) // Opsional kalau ada

        // Tampilkan data awal dulu biar gak kosong
        updateUI(tvTitle, tvDate, tvContent, initialData)

        // --- SOLUSI: DENGARKAN PERUBAHAN DATA (REAL-TIME) ---
        db.collection("announcements").document(announcementId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null && snapshot.exists()) {
                    // Data berubah di server? Ambil yang baru!
                    val freshAnnouncement = snapshot.toObject(Announcement::class.java)
                    if (freshAnnouncement != null) {
                        // KITA SET ID MANUAL KARENA toObject KADANG GAK BAWA ID
                        val fixedData = freshAnnouncement.copy(id = snapshot.id)
                        currentAnnouncement = fixedData

                        // Update tampilan
                        updateUI(tvTitle, tvDate, tvContent, fixedData)
                    }
                }
            }

        btnEdit.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("announcement", currentAnnouncement) // Kirim data TERBARU
            }
            findNavController().navigate(R.id.action_announcementDetail_to_editAnnouncement, bundle)
        }

        btnDelete.setOnClickListener { confirmDelete() }

        // Kalau tombol back ditekan
        btnBack?.setOnClickListener { findNavController().popBackStack() }
    }

    private fun updateUI(tvTitle: TextView, tvDate: TextView, tvContent: TextView, data: Announcement) {
        tvTitle.text = data.title
        tvContent.text = data.content
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        tvDate.text = sdf.format(data.date)
    }

    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Pengumuman")
            .setMessage("Yakin ingin menghapus pengumuman ini?")
            .setPositiveButton("Hapus") { _, _ ->
                db.collection("announcements").document(announcementId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}