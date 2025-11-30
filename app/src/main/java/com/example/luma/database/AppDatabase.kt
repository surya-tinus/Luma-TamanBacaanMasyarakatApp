package com.example.luma.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// PENTING:
// 1. Tambahkan Book::class ke entities
// 2. Ubah version dari 1 menjadi 2 (karena struktur tabel berubah)
@Database(entities = [User::class, Book::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun bookDao(): BookDao // <-- Tambahkan ini

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Saat development, kita izinkan .fallbackToDestructiveMigration()
                // Supaya kalau struktur tabel berubah, database lama dihapus & dibuat baru (biar gak error)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "luma_database"
                )
                    .fallbackToDestructiveMigration() // <-- Tambahkan ini biar aman saat update versi db
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}