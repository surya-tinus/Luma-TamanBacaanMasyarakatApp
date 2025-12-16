package com.example.luma

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class MemberDetailFragment : Fragment() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter

    // UI Baru untuk Analytics
    private lateinit var cvActiveStatus: CardView
    private lateinit var tvActiveCount: TextView

    private val historyList = mutableListOf<LoanHistory>()
    private val db = FirebaseFirestore.getInstance()

    // Model Data Lokal
    data class LoanHistory(
        val title: String,
        val date: String,
        val status: String
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_member_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvName = view.findViewById<TextView>(R.id.tvDetailName)
        val tvEmail = view.findViewById<TextView>(R.id.tvDetailEmail)
        val tvRole = view.findViewById<TextView>(R.id.tvDetailRole)
        val tvPhone = view.findViewById<TextView>(R.id.tvDetailPhone)
        val btnBack = view.findViewById<Button>(R.id.btnBackMember)

        // Init UI Baru
        cvActiveStatus = view.findViewById(R.id.cvActiveStatus)
        tvActiveCount = view.findViewById(R.id.tvActiveCount)

        rvHistory = view.findViewById(R.id.rvMemberHistory)
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        adapter = HistoryAdapter(historyList)
        rvHistory.adapter = adapter

        // AMBIL DATA DARI BUNDLE (KIRIMAN DARI LIST MEMBER)
        // Kita butuh 'name' (Username) karena itu yang disimpan di tabel loans sebagai userId
        val memberUsername = arguments?.getString("name")

        arguments?.let {
            tvName.text = it.getString("name", "—")
            tvEmail.text = "Email: ${it.getString("email", "—")}"
            tvRole.text = "Role: ${it.getString("role", "—")}"
            tvPhone.text = "No. HP: ${it.getString("phone", "-")}"
        }

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // PERBAIKAN UTAMA:
        // Gunakan memberUsername (contoh: "luma1") untuk query, BUKAN memberId (UID)
        if (memberUsername != null) {
            fetchMemberLoans(memberUsername)
        } else {
            Toast.makeText(context, "Username tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchMemberLoans(username: String) {
        // Log Debugging
        Log.d("DEBUG_LOAN", "Mencari pinjaman untuk Username: $username")

        // Query ke database: cari dokumen di 'loans' yang userId == username
        db.collection("loans")
            .whereEqualTo("userId", username)
            .get()
            .addOnSuccessListener { documents ->

                Log.d("DEBUG_LOAN", "Ditemukan: ${documents.size()} buku")

                historyList.clear()
                var activeLoanCount = 0

                for (doc in documents) {
                    val title = doc.getString("bookTitle") ?: "Unknown Title"
                    val status = doc.getString("status") ?: "Dipinjam"

                    // Format Tanggal
                    val timestamp = doc.getTimestamp("loanDate")
                    val date = if (timestamp != null) {
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(timestamp.toDate())
                    } else "-"

                    // LOGIC HITUNG YANG AKTIF
                    // Kita cek kalau statusnya "active" (sesuai database kamu) atau "Dipinjam"
                    if (status.equals("active", ignoreCase = true) ||
                        status.equals("Dipinjam", ignoreCase = true) ||
                        status.equals("Menunggu Persetujuan", ignoreCase = true)) {
                        activeLoanCount++
                    }

                    historyList.add(LoanHistory(title, date, status))
                }

                adapter.notifyDataSetChanged()

                // Update Kartu Analytics di atas
                updateStatusCard(activeLoanCount)
            }
            .addOnFailureListener {
                Log.e("DEBUG_LOAN", "Error ambil data: ${it.message}")
                Toast.makeText(context, "Gagal mengambil data peminjaman", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateStatusCard(count: Int) {
        if (count > 0) {
            // ADA BUKU DIPINJAM (WARNING)
            tvActiveCount.text = "Sedang Meminjam: $count Buku"

            // Ubah warna kartu jadi Oranye/Kuning
            cvActiveStatus.setCardBackgroundColor(Color.parseColor("#FFF3E0"))
            tvActiveCount.setTextColor(Color.parseColor("#E65100"))
        } else {
            // AMAN (HIJAU)
            tvActiveCount.text = "Aman (Tidak ada pinjaman)"

            // Ubah warna kartu jadi Hijau Muda
            cvActiveStatus.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
            tvActiveCount.setTextColor(Color.parseColor("#2E7D32"))
        }
    }

    // ADAPTER RECYCLERVIEW
    class HistoryAdapter(private val list: List<LoanHistory>) : RecyclerView.Adapter<HistoryAdapter.VH>() {

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvTitle: TextView = v.findViewById(android.R.id.text1)
            val tvInfo: TextView = v.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            return VH(v)
        }

        override fun getItemCount() = list.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = list[position]
            holder.tvTitle.text = item.title

            // Format status biar rapi (Huruf depan besar)
            val formattedStatus = item.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            holder.tvInfo.text = "${item.date} • $formattedStatus"

            // Warna Teks Status
            if (item.status.equals("returned", ignoreCase = true) || item.status.equals("dikembalikan", ignoreCase = true)) {
                holder.tvInfo.setTextColor(Color.parseColor("#4CAF50")) // Hijau
            } else {
                holder.tvInfo.setTextColor(Color.parseColor("#FF9800")) // Oranye
            }
        }
    }
}