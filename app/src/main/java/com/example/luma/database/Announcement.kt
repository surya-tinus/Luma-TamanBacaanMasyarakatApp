package com.example.luma.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Announcement(
    var id: String = "",
    val title: String = "",
    val content: String = "",
    val date: Date = Date(),
    val type: String = "info" // 'info', 'event', 'new_book'
) : Parcelable