package com.example.luma.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    // Fungsi untuk REGISTER: Menambahkan user baru
    @Insert
    suspend fun insertUser(user: User)

    // Fungsi untuk LOGIN: Mencari user berdasarkan username dan password
    // Jika ditemukan, akan mengembalikan data User. Jika salah, mengembalikan null.
    @Query("SELECT * FROM user_table WHERE username = :username AND password = :password")
    suspend fun login(username: String, password: String): User?

    // Fungsi tambahan: Cek apakah username sudah ada (biar tidak duplikat saat register)
    @Query("SELECT * FROM user_table WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?
}