package com.example.luma.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "book_table")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "author")
    val author: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "synopsis")
    val synopsis: String,

    @ColumnInfo(name = "stock")
    val stock: Int,

    @ColumnInfo(name = "rating")
    val rating: Double = 0.0,

    @ColumnInfo(name = "image_path") // Kita ganti namanya jadi image_path
    val imagePath: String = "" // Tipe String, default-nya kosong
) : Parcelable