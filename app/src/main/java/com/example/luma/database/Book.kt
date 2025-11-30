package com.example.luma.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// @Entity menandakan ini adalah tabel database
// @Parcelize supaya data buku bisa dikirim antar-Activity (misal saat klik buku untuk lihat detail)
@Parcelize
@Entity(tableName = "book_table")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "author")
    val author: String, // Penting untuk fitur "Cari berdasarkan Penulis"

    @ColumnInfo(name = "category")
    val category: String, // Penting untuk fitur "Cari berdasarkan Kategori"

    @ColumnInfo(name = "synopsis") // Dulu 'description', kita ganti 'synopsis' biar lebih keren
    val synopsis: String,

    @ColumnInfo(name = "stock")
    val stock: Int, // Penting untuk logika "Bisa dipinjam jika stok > 0"

    @ColumnInfo(name = "rating")
    val rating: Double = 0.0, // Untuk fitur "Rating buku"

    @ColumnInfo(name = "image_res_id")
    val imageResId: Int // Kita pakai ID Gambar (R.drawable.xxx) dulu biar mudah
) : Parcelable