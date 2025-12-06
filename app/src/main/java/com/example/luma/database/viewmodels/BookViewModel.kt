package com.example.luma.database.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.luma.database.Book
import com.example.luma.database.Loan
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class BookViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val booksCollection = db.collection("books")
    private val loansCollection = db.collection("loans")

    // --- LiveData ---
    private val _allBooks = MutableLiveData<List<Book>>()
    val allBooks: LiveData<List<Book>> = _allBooks

    private val _userLoans = MutableLiveData<List<Loan>>()
    val userLoans: LiveData<List<Loan>> = _userLoans

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _borrowStatus = MutableLiveData<String?>()
    val borrowStatus: LiveData<String?> = _borrowStatus

    init {
        fetchBooksRealtime()
    }

    // --- 1. AMBIL SEMUA BUKU (HOME) ---
    private fun fetchBooksRealtime() {
        _isLoading.value = true
        booksCollection.addSnapshotListener { snapshots, error ->
            if (error != null) {
                _isLoading.value = false
                return@addSnapshotListener
            }
            val listBuku = mutableListOf<Book>()
            if (snapshots != null) {
                for (doc in snapshots) {
                    val buku = doc.toObject(Book::class.java)
                    buku.id = doc.id
                    listBuku.add(buku)
                }
            }
            _allBooks.value = listBuku
            _isLoading.value = false
        }
    }

    // --- 2. CARI BUKU (SEARCH) ---
    fun searchBooks(query: String): List<Book> {
        val currentList = _allBooks.value ?: emptyList()
        return currentList.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.author.contains(query, ignoreCase = true)
        }
    }

    // --- 3. PINJAM BUKU ---
    fun borrowBook(book: Book, userId: String) {
        _isLoading.value = true
        val bookRef = booksCollection.document(book.id)
        val loanRef = loansCollection.document()

        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val due = calendar.time

        db.runTransaction { transaction ->
            val snapshot = transaction.get(bookRef)
            val currentStock = snapshot.getLong("stock")?.toInt() ?: 0

            if (currentStock <= 0) throw Exception("Stok habis!")

            transaction.update(bookRef, "stock", currentStock - 1)

            val newLoan = Loan(
                id = loanRef.id,
                userId = userId,
                bookId = book.id,
                bookTitle = book.title,
                bookAuthor = book.author,
                bookImage = book.imagePath,
                borrowDate = today,
                dueDate = due,
                status = "active"
            )
            transaction.set(loanRef, newLoan)
        }
            .addOnSuccessListener {
                _isLoading.value = false
                _borrowStatus.value = "Berhasil meminjam! Cek menu History."
            }
            .addOnFailureListener {
                _isLoading.value = false
                _borrowStatus.value = "Gagal: ${it.message}"
            }
    }

    // --- 4. AMBIL HISTORY PEMINJAMAN ---
    fun fetchUserLoans(userId: String) {
        _isLoading.value = true
        loansCollection.whereEqualTo("userId", userId)
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

    // --- 5. KEMBALIKAN BUKU (RETURN) ---
    fun returnBook(loan: Loan) {
        _isLoading.value = true
        val bookRef = booksCollection.document(loan.bookId)
        val loanRef = loansCollection.document(loan.id)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(bookRef)
            val currentStock = snapshot.getLong("stock")?.toInt() ?: 0

            transaction.update(bookRef, "stock", currentStock + 1)
            transaction.update(loanRef, "status", "returned")
        }
            .addOnSuccessListener {
                _isLoading.value = false
                _borrowStatus.value = "Buku berhasil dikembalikan!"
            }
            .addOnFailureListener {
                _isLoading.value = false
                _borrowStatus.value = "Gagal mengembalikan: ${it.message}"
            }
    }

    // --- 6. KIRIM REVIEW (RATING) ---
    fun submitReview(loan: Loan, rating: Float, review: String) {
        _isLoading.value = true
        val loanRef = loansCollection.document(loan.id)

        val updates = mapOf(
            "userRating" to rating,
            "userReview" to review
        )

        loanRef.update(updates)
            .addOnSuccessListener {
                _isLoading.value = false
                _borrowStatus.value = "Terima kasih atas ulasanmu!"
            }
            .addOnFailureListener {
                _isLoading.value = false
                _borrowStatus.value = "Gagal kirim ulasan: ${it.message}"
            }
    }

    // --- 7. SEEDING DATA (Admin Tool) ---
    fun seedData() {
        _isLoading.value = true
        booksCollection.get().addOnSuccessListener { snapshot ->
            val batch = db.batch()
            for (doc in snapshot) {
                batch.delete(doc.reference)
            }
            batch.commit().addOnSuccessListener {
                insertNewBooks()
            }
        }
    }

    private fun insertNewBooks() {
        val dummyImage = ""
        val listBuku = listOf(
            Book(title = "Laskar Pelangi", author = "Andrea Hirata", category = "Fiction", synopsis = "Kisah anak Belitong...", stock = 5, rating = 4.8, imagePath = dummyImage),
            Book(title = "Harry Potter", author = "J.K. Rowling", category = "Fiction", synopsis = "Petualangan sihir...", stock = 8, rating = 4.9, imagePath = dummyImage),
            Book(title = "Dilan 1990", author = "Pidi Baiq", category = "Romance", synopsis = "Kisah cinta SMA...", stock = 10, rating = 4.7, imagePath = dummyImage),
            Book(title = "KKN Desa Penari", author = "SimpleMan", category = "Horror", synopsis = "Kejadian mistis...", stock = 6, rating = 4.5, imagePath = dummyImage),
            Book(title = "Marmut Merah Jambu", author = "Raditya Dika", category = "Comedy", synopsis = "Cinta pertama...", stock = 7, rating = 4.5, imagePath = dummyImage),
            Book(title = "Atomic Habits", author = "James Clear", category = "Science", synopsis = "Kebiasaan baik...", stock = 12, rating = 4.9, imagePath = dummyImage)
        )
        for (buku in listBuku) {
            booksCollection.add(buku)
        }
        _isLoading.value = false
    }

    fun resetBorrowStatus() {
        _borrowStatus.value = null
    }
}