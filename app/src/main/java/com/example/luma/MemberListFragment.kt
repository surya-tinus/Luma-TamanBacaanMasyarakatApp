package com.example.luma

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment

class MemberListFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_member_list, container, false)

        val members = listOf("Rina", "Budi", "Sinta", "Dewi")
        val listView = view.findViewById<ListView>(R.id.listMembers)
        listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, members)

        return view
    }
}