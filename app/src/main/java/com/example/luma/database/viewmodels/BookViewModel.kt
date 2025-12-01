package com.example.luma.database.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.luma.database.AppDatabase
import com.example.luma.database.Book
import com.example.luma.database.BookDao

class BookViewModel(application: Application) : AndroidViewModel(application) {

    private val bookDao: BookDao
    // LiveData ini yang akan dipantau oleh MainActivity
    val allBooks: LiveData<List<Book>>

    init {
        // Kita kirim 'viewModelScope' sesuai perubahan terakhir di AppDatabase
        val database = AppDatabase.getDatabase(application, viewModelScope)
        bookDao = database.bookDao()

        // Ambil semua buku secara otomatis
        allBooks = bookDao.getAllBooks()
    }

    // Fungsi untuk Mencari Buku (Judul, Penulis, Kategori)
    // Nanti dipanggil saat user ketik di Search Bar
    fun searchBooks(query: String): LiveData<List<Book>> {
        val searchQuery = "%$query%" // Tambah % biar pencarian fleksibel
        return bookDao.searchBooks(searchQuery)
    }
}