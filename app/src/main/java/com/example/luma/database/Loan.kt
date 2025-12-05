package com.example.luma.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Loan(
    var id: String = "",
    val userId: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val bookAuthor: String = "", // <--- TAMBAHAN BARU
    val bookImage: String = "",
    val borrowDate: Date? = null,
    val dueDate: Date? = null,
    val status: String = "active"
) : Parcelable