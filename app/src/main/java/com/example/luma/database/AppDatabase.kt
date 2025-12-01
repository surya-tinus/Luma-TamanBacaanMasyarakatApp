package com.example.luma.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.luma.R // Import R agar bisa akses gambar logo/buku

@Database(entities = [User::class, Book::class], version = 3)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun bookDao(): BookDao // Tambahkan akses ke BookDao

    // --- LOGIKA SEEDING DATA ---
    private class BookDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {

        // Fungsi ini dipanggil pas database PERTAMA KALI dibuat
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.bookDao())
                }
            }
        }

        // Isi data dummy di sini

        // Di dalam AppDatabase.kt

        suspend fun populateDatabase(bookDao: BookDao) {
            // Hapus data lama biar bersih dan terganti dengan yang baru
            bookDao.deleteAll() // Pastikan fungsi deleteAll() sudah ada di BookDao, kalau belum tambah dulu ya (lihat bawah)

            // Kita kosongkan path gambar agar Adapter otomatis pakai gambar default (placeholder)
            val defaultImage = ""

            val listBuku = listOf(
                // --- FICTION ---
                Book(
                    title = "Laskar Pelangi",
                    author = "Andrea Hirata",
                    category = "Fiction",
                    synopsis = "Kisah perjuangan anak-anak Belitong mengejar mimpi di tengah keterbatasan.",
                    stock = 5,
                    rating = 4.8,
                    imagePath = defaultImage
                ),
                Book(
                    title = "Harry Potter and the Sorcerer's Stone",
                    author = "J.K. Rowling",
                    category = "Fiction",
                    synopsis = "Petualangan Harry Potter, seorang anak yatim piatu yang ternyata adalah penyihir.",
                    stock = 8,
                    rating = 4.9,
                    imagePath = defaultImage
                ),
                Book(
                    title = "Bumi Manusia",
                    author = "Pramoedya Ananta Toer",
                    category = "Fiction",
                    synopsis = "Kisah Minke, pribumi cerdas yang melawan ketidakadilan kolonial Belanda.",
                    stock = 3,
                    rating = 4.9,
                    imagePath = defaultImage
                ),
                Book(
                    title = "The Hobbit",
                    author = "J.R.R. Tolkien",
                    category = "Fiction",
                    synopsis = "Petualangan Bilbo Baggins dalam merebut kembali kerajaan kurcaci.",
                    stock = 4,
                    rating = 4.7,
                    imagePath = defaultImage
                ),

                // --- ROMANCE ---
                Book(
                    title = "Dilan 1990",
                    author = "Pidi Baiq",
                    category = "Romance",
                    synopsis = "Kisah cinta remaja SMA di Bandung antara Dilan sang panglima tempur dan Milea.",
                    stock = 10,
                    rating = 4.6,
                    imagePath = defaultImage
                ),
                Book(
                    title = "The Notebook",
                    author = "Nicholas Sparks",
                    category = "Romance",
                    synopsis = "Kisah cinta abadi antara Noah dan Allie yang teruji oleh waktu dan penyakit.",
                    stock = 2,
                    rating = 4.5,
                    imagePath = defaultImage
                ),
                Book(
                    title = "Antologi Rasa",
                    author = "Ika Natassa",
                    category = "Romance",
                    synopsis = "Kisah persahabatan dan cinta segitiga antara Keara, Harris, dan Ruly.",
                    stock = 5,
                    rating = 4.2,
                    imagePath = defaultImage
                ),
                Book(
                    title = "Pride and Prejudice",
                    author = "Jane Austen",
                    category = "Romance",
                    synopsis = "Klasik romantis tentang Elizabeth Bennet dan Mr. Darcy.",
                    stock = 3,
                    rating = 4.8,
                    imagePath = defaultImage
                ),

                // --- HORROR ---
                Book(
                    title = "KKN di Desa Penari",
                    author = "SimpleMan",
                    category = "Horror",
                    synopsis = "Sekelompok mahasiswa mengalami kejadian mistis saat KKN di desa terpencil.",
                    stock = 6,
                    rating = 4.3,
                    imagePath = defaultImage
                ),
                Book(
                    title = "Danur",
                    author = "Risa Saraswati",
                    category = "Horror",
                    synopsis = "Kisah nyata Risa yang bisa melihat hantu dan berteman dengan mereka.",
                    stock = 4,
                    rating = 4.4,
                    imagePath = defaultImage
                ),
                Book(
                    title = "It",
                    author = "Stephen King",
                    category = "Horror",
                    synopsis = "Teror badut Pennywise yang menghantui anak-anak di kota Derry.",
                    stock = 2,
                    rating = 4.7,
                    imagePath = defaultImage
                ),

                // --- COMEDY ---
                Book(
                    title = "Marmut Merah Jambu",
                    author = "Raditya Dika",
                    category = "Comedy",
                    synopsis = "Kumpulan cerita komedi tentang cinta pertama dan masa sekolah.",
                    stock = 7,
                    rating = 4.5,
                    imagePath = defaultImage
                ),
                Book(
                    title = "Manusia Setengah Salmon",
                    author = "Raditya Dika",
                    category = "Comedy",
                    synopsis = "Cerita kocak tentang perpindahan dan perubahan dalam hidup.",
                    stock = 5,
                    rating = 4.4,
                    imagePath = defaultImage
                ),
                Book(
                    title = "Good Omens",
                    author = "Neil Gaiman & Terry Pratchett",
                    category = "Comedy",
                    synopsis = "Malaikat dan Iblis bekerja sama mencegah kiamat karena mereka suka hidup di bumi.",
                    stock = 3,
                    rating = 4.8,
                    imagePath = defaultImage
                ),

                // --- SELF IMPROVEMENT (Masuk kategori 'Science' atau lainnya di chip kamu) ---
                Book(
                    title = "Atomic Habits",
                    author = "James Clear",
                    category = "Science",
                    synopsis = "Cara perubahan kecil memberikan hasil yang luar biasa dalam hidup.",
                    stock = 15,
                    rating = 4.9,
                    imagePath = defaultImage
                ),
                Book(
                    title = "Sapiens",
                    author = "Yuval Noah Harari",
                    category = "Science",
                    synopsis = "Riwayat singkat umat manusia dari zaman batu hingga modern.",
                    stock = 4,
                    rating = 4.8,
                    imagePath = defaultImage
                )
            )

            bookDao.insertAll(listBuku)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Kita tambah parameter 'scope' untuk menjalankan seeding di background
        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "luma_database"
                )
                    // Pasang Callback Seeding di sini
                    .addCallback(BookDatabaseCallback(scope))
                    // Hancurkan db lama jika versi berubah (biar gak crash saat update struktur)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}