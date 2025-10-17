package com.example.projectgroup7

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class MemberDetailFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_member_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvName = view.findViewById<TextView>(R.id.tvDetailName)
        val tvEmail = view.findViewById<TextView>(R.id.tvDetailEmail)
        val tvJoined = view.findViewById<TextView>(R.id.tvDetailJoined)
        val tvBorrowed = view.findViewById<TextView>(R.id.tvDetailBorrowed)
        val btnBack = view.findViewById<Button>(R.id.btnBackMember)

        arguments?.let {
            tvName.text = it.getString("name", "—")
            tvEmail.text = "Email: ${it.getString("email", "—")}"
            tvJoined.text = "Joined: ${it.getString("joined", "—")}"
            tvBorrowed.text = "Borrowed books: ${it.getInt("borrowed", 0)}"
        }

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }
}