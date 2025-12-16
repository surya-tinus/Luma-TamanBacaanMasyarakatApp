package com.example.luma

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MembersFragment : Fragment() {

    // 1. UBAH MODEL DATA (ID jadi String karena Firebase ID itu text)
    data class Member(
        val id: String,
        val name: String,
        val email: String,
        val role: String,
        val phone: String // Tambahan info kalau ada
    )

    private val members = mutableListOf<Member>()
    private lateinit var rvMembers: RecyclerView
    private lateinit var adapter: MemberAdapter
    private val db = FirebaseFirestore.getInstance() // Init Firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvMembers = view.findViewById(R.id.rvMembers)
        rvMembers.layoutManager = LinearLayoutManager(requireContext())

        // Setup Adapter
        adapter = MemberAdapter(members) { member ->
            // Navigasi ke Detail
            val bundle = Bundle().apply {
                putString("id", member.id) // Kirim ID asli Firebase
                putString("name", member.name)
                putString("email", member.email)
                putString("role", member.role)
                putString("phone", member.phone)
            }

            // Pastikan ID Action ini bener di nav_graph kamu
            try {
                findNavController().navigate(R.id.action_membersFragment_to_memberDetailFragment, bundle)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Belum ada jalur navigasi di nav_graph", Toast.LENGTH_SHORT).show()
            }
        }
        rvMembers.adapter = adapter

        // PANGGIL FUNGSI AMBIL DATA
        fetchMembersFromFirebase()
    }

    private fun fetchMembersFromFirebase() {
        // Ambil semua user yang role-nya BUKAN admin (jadi member doang)
        // Atau ambil semua user: db.collection("users").get()

        db.collection("users")
            //.whereEqualTo("role", "member") // Uncomment kalo mau filter member aja
            .get()
            .addOnSuccessListener { documents ->
                members.clear()
                for (document in documents) {
                    // Mapping data dari Firestore ke Object Member
                    // Pastikan nama field ("username", "email") SAMA PERSIS dengan di Firebase Console
                    val id = document.id
                    val name = document.getString("username") ?: "No Name"
                    val email = document.getString("email") ?: "-"
                    val role = document.getString("role") ?: "member"
                    val phone = document.getString("phone") ?: "-"

                    members.add(Member(id, name, email, role, phone))
                }
                adapter.notifyDataSetChanged() // Refresh List

                if (members.isEmpty()) {
                    Toast.makeText(context, "Belum ada member terdaftar", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Gagal ambil data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ADAPTER (Inner Class)
    class MemberAdapter(
        private val list: List<Member>,
        private val onClick: (Member) -> Unit
    ) : RecyclerView.Adapter<MemberAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvName = view.findViewById<TextView>(R.id.tvMemberName)
            val tvInfo = view.findViewById<TextView>(R.id.tvMemberInfo)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_member, parent, false)
            return VH(v)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val m = list[position]
            holder.tvName.text = m.name
            // Tampilin Email dan Role di baris bawah
            holder.tvInfo.text = "${m.email} (${m.role})"

            holder.itemView.setOnClickListener { onClick(m) }
        }
    }
}