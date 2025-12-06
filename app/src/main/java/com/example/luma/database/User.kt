package com.example.luma.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var id: String = "", // UID dari Firebase Auth
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val birthdate: String = "",
    val role: String = "member"
) : Parcelable