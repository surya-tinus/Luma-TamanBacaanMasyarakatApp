package com.example.luma.database.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.luma.database.Book
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import java.util.Calendar
import java.util.Date
import com.example.luma.database.Loan

class BookViewModel(application: Application) : AndroidViewModel(application) {

    // Ganti inisialisasi jadi gaya klasik (getInstance)
    // Ini anti-error karena tidak butuh extension KTX
    private val db = FirebaseFirestore.getInstance()
    private val booksCollection = db.collection("books")

    private val _allBooks = MutableLiveData<List<Book>>()
    val allBooks: LiveData<List<Book>> = _allBooks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        fetchBooksRealtime()
    }

    private fun fetchBooksRealtime() {
        _isLoading.value = true

        booksCollection.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.e("BookViewModel", "Error: ", error)
                _isLoading.value = false
                return@addSnapshotListener
            }

            val listBuku = mutableListOf<Book>()
            if (snapshots != null) {
                for (document in snapshots) {
                    // toObject tetap bisa dipakai
                    val buku = document.toObject<Book>()
                    buku.id = document.id
                    listBuku.add(buku)
                }
            }

            _allBooks.value = listBuku
            _isLoading.value = false
        }
    }

    fun seedData() {
        val dummyImage = ""

        val listBuku = listOf(
            Book(title = "Laskar Pelangi", author = "Andrea Hirata", category = "Fiction", synopsis = "Kisah anak Belitong...", stock = 5, rating = 4.8, imagePath = dummyImage),
            Book(title = "Atomic Habits", author = "James Clear", category = "Science", synopsis = "Membangun kebiasaan baik...", stock = 10, rating = 4.9, imagePath = dummyImage),
            Book(title = "KKN di Desa Penari", author = "SimpleMan", category = "Horror", synopsis = "Teror di desa terpencil...", stock = 3, rating = 4.5, imagePath = dummyImage)
        )

        for (buku in listBuku) {
            booksCollection.add(buku)
        }
    }

    fun searchBooks(query: String): List<Book> {
        val currentList = _allBooks.value ?: emptyList()
        return currentList.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.author.contains(query, ignoreCase = true)
        }
    }

    private val _borrowStatus = MutableLiveData<String?>()
    val borrowStatus: LiveData<String?> = _borrowStatus

    fun resetBorrowStatus() {
        _borrowStatus.value = null
    }

    // --- FUNGSI PINJAM BUKU ---
    // ... di dalam BookViewModel ...

    fun borrowBook(book: Book, userId: String) {
        _isLoading.value = true

        val bookRef = db.collection("books").document(book.id)
        val loanRef = db.collection("loans").document() // Definisi loanRef

        // PENTING: Siapkan tanggal di luar transaksi biar rapi
        val calendar = java.util.Calendar.getInstance()
        val today = calendar.time
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 7)
        val due = calendar.time

        db.runTransaction { transaction ->
            // 1. Baca data terbaru
            val snapshot = transaction.get(bookRef)
            val currentStock = snapshot.getLong("stock")?.toInt() ?: 0

            // 2. Cek Stok
            if (currentStock <= 0) {
                throw Exception("Yah, stok buku habis duluan!")
            }

            // 3. Kurangi Stok
            transaction.update(bookRef, "stock", currentStock - 1)

            // 4. Siapkan Data Peminjaman
            // Variabel 'loanRef', 'today', 'due' sekarang bisa diakses karena ada di scope atas
            val newLoan = Loan(
                id = loanRef.id,
                userId = userId,
                bookId = book.id,
                bookTitle = book.title,
                bookImage = book.imagePath,
                borrowDate = today,
                dueDate = due,
                status = "active"
            )

            // 5. Simpan Data Peminjaman
            transaction.set(loanRef, newLoan)
        }
            .addOnSuccessListener {
                _isLoading.value = false
                _borrowStatus.value = "Berhasil meminjam! Cek menu History."
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _borrowStatus.value = "Gagal: ${e.message}"
            }
    }

    // LiveData untuk menampung list peminjaman user
    private val _userLoans = MutableLiveData<List<Loan>>()
    val userLoans: LiveData<List<Loan>> = _userLoans

    // Fungsi Ambil Data Peminjaman User
    fun fetchUserLoans(userId: String) {
        _isLoading.value = true

        // Cari di tabel 'loans' yang 'userId'-nya sama dengan yang login
        db.collection("loans")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val loans = mutableListOf<Loan>()
                if (snapshots != null) {
                    for (doc in snapshots) {
                        val loan = doc.toObject(Loan::class.java)
                        loan.id = doc.id
                        loans.add(loan)
                    }
                }
                _userLoans.value = loans
                _isLoading.value = false
            }
    }
}