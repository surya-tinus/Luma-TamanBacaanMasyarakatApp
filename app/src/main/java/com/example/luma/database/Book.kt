package com.example.luma.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Hapus semua anotasi @Entity, @PrimaryKey, @ColumnInfo
// Firebase butuh constructor kosong (default value) agar bisa otomatis convert data
@Parcelize
data class Book(
    // ID di Firebase itu String unik (acak dari server), bukan Int
    // Kita set var agar bisa diisi setelah data diambil
    var id: String = "",

    val title: String = "",
    val author: String = "",
    val category: String = "",
    val synopsis: String = "",
    val stock: Int = 0,
    val rating: Double = 0.0,
    val imagePath: String = ""
) : Parcelable