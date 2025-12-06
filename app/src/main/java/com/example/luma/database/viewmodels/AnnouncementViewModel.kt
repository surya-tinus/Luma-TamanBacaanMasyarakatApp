package com.example.luma.database.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.luma.database.Announcement
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AnnouncementViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val announcementCollection = db.collection("announcements")

    private val _announcements = MutableLiveData<List<Announcement>>()
    val announcements: LiveData<List<Announcement>> = _announcements

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        fetchAnnouncements()
    }

    private fun fetchAnnouncements() {
        _isLoading.value = true

        // Ambil data realtime, urutkan dari tanggal terbaru
        announcementCollection
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val list = mutableListOf<Announcement>()
                if (snapshots != null) {
                    for (doc in snapshots) {
                        val ann = doc.toObject(Announcement::class.java)
                        ann.id = doc.id
                        list.add(ann)
                    }
                }
                _announcements.value = list
                _isLoading.value = false
            }
    }
}