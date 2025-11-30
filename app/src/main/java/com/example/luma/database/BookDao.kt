package com.example.luma.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.lifecycle.LiveData

@Dao
interface BookDao {
    // 1. Masukkan buku baru (Buat admin nanti atau seeding data)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)

    // 2. Masukkan banyak buku sekaligus (biar cepat isi data awal)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(books: List<Book>)

    // 3. Ambil SEMUA buku untuk ditampilkan di Home
    @Query("SELECT * FROM book_table ORDER BY title ASC")
    fun getAllBooks(): LiveData<List<Book>>

    // 4. Fitur PENCARIAN (Judul, Penulis, atau Kategori)
    // :query nanti akan diganti teks yang diketik user
    @Query("SELECT * FROM book_table WHERE title LIKE :query OR author LIKE :query OR category LIKE :query")
    fun searchBooks(query: String): LiveData<List<Book>>

    // 5. Ambil detail satu buku berdasarkan ID (saat diklik)
    @Query("SELECT * FROM book_table WHERE id = :id")
    suspend fun getBookById(id: Int): Book
}