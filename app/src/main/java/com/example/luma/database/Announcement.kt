package com.example.luma.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Announcement(
    var id: String = "",
    var title: String = "",
    var content: String = "",
    var date: Date = Date(),
    var type: String = "info",
    var stability: Int = 0
) : Parcelable
