package com.example.luma

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.example.luma.database.Announcement
import com.example.luma.database.viewmodels.AnnouncementViewModel

class ManageAnnouncementsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnAdd: Button
    private lateinit var adapter: AnnouncementAdapter

    private val viewModel: AnnouncementViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manage_announcements, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        recyclerView = view.findViewById(R.id.rvAdminAnnouncements)
        progressBar = view.findViewById(R.id.progressAnnouncement)
        btnAdd = view.findViewById(R.id.btnAddAnnouncement)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = AnnouncementAdapter(emptyList()) { clickedItem ->
            openAnnouncementDetail(clickedItem)
        }

        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            findNavController().navigate(R.id.action_manageAnnouncements_to_addAnnouncement)
        }

        viewModel.announcements.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    private fun openAnnouncementDetail(item: Announcement) {
        val bundle = Bundle().apply {
            putParcelable("announcement", item)
        }
        findNavController().navigate(
            R.id.action_manageAnnouncements_to_announcementDetail,
            bundle
        )
    }
}