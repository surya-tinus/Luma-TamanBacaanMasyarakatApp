package com.example.projectgroup7

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MembersFragment : Fragment() {

    data class Member(
        val id: Int,
        val name: String,
        val email: String,
        val joined: String,
        val borrowedCount: Int
    )

    private val members = mutableListOf<Member>()
    private lateinit var rvMembers: RecyclerView
    private lateinit var adapter: MemberAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvMembers = view.findViewById(R.id.rvMembers)
        rvMembers.layoutManager = LinearLayoutManager(requireContext())

        // Dummy data anggota
        for (i in 1..12) {
            members.add(
                Member(
                    i,
                    "Member $i",
                    "member$i@example.com",
                    "2024-0${(i % 9) + 1}-0${(i % 27) + 1}",
                    i % 4
                )
            )
        }

        adapter = MemberAdapter(members) { member ->
            // Navigasi ke MemberDetailFragment
            val bundle = Bundle().apply {
                putInt("id", member.id)
                putString("name", member.name)
                putString("email", member.email)
                putString("joined", member.joined)
                putInt("borrowed", member.borrowedCount)
            }

            // Pastikan ada action di admin_nav_graph.xml dari MembersFragment ke MemberDetailFragment
            findNavController().navigate(R.id.action_membersFragment_to_memberDetailFragment, bundle)
        }

        rvMembers.adapter = adapter
    }

    class MemberAdapter(
        private val list: List<Member>,
        private val onClick: (Member) -> Unit
    ) : RecyclerView.Adapter<MemberAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvName = view.findViewById<android.widget.TextView>(R.id.tvMemberName)
            val tvInfo = view.findViewById<android.widget.TextView>(R.id.tvMemberInfo)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_member, parent, false)
            return VH(v)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val m = list[position]
            holder.tvName.text = m.name
            holder.tvInfo.text = m.email
            holder.itemView.setOnClickListener { onClick(m) }
        }
    }
}