package com.example.luma

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luma.database.Book
import com.google.firebase.firestore.FirebaseFirestore

class ManageBooksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookAdminAdapter
    private lateinit var addButton: Button

    private val books = mutableListOf<BookLocal>()
    private val db = FirebaseFirestore.getInstance().collection("books")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_manage_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewBooks)
        addButton = view.findViewById(R.id.btnAddBook)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = BookAdminAdapter(
            list = books,
            onClick = { book ->
                // Aksi kalau gambar buku diklik (bisa dikosongin atau buka detail)
            },
            onEdit = { book -> showEditForm(book) },   // Aksi Edit
            onDelete = { book -> confirmDelete(book) } // Aksi Hapus
        )
        recyclerView.adapter = adapter

        addButton.setOnClickListener {
            // Navigasi ke Halaman Tambah (AddBookFragment)
            try {
                findNavController().navigate(R.id.action_booksFragment_to_addBookFragment)
            } catch (e: Exception) {
                try {
                    // Coba nama action alternatif (sesuai graph admin kamu)
                    findNavController().navigate(R.id.action_booksFragment_to_addBookFragment)
                } catch (e2: Exception) {
                    // Fallback tembak ID Fragment langsung
                    findNavController().navigate(R.id.addBookFragment)
                }
            }
        }

        fetchBooks()
    }

    // =========================================================
    // 1. FETCH BOOKS (PERBAIKAN: AMBIL AUTHOR & SINOPSIS)
    // =========================================================
    private fun fetchBooks() {
        db.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            if (snapshot != null) {
                books.clear()
                for (doc in snapshot.documents) {
                    // Pastikan class 'Book' (database) kamu punya field author & synopsis ya!
                    val bookFirestore = doc.toObject(Book::class.java)

                    if (bookFirestore != null) {
                        // Masukkan data lengkap ke list lokal
                        books.add(
                            BookLocal(
                                id = doc.id,
                                title = bookFirestore.title,
                                author = bookFirestore.author,      // <--- AMBIL DATA ASLI
                                category = bookFirestore.category,
                                synopsis = bookFirestore.synopsis,  // <--- AMBIL DATA ASLI
                                imageUrl = bookFirestore.imagePath,
                                stock = bookFirestore.stock
                            )
                        )
                    }
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    // =========================================================
    // 2. EDIT BOOK (PERBAIKAN: KIRIM DATA ASLI KE FORM)
    // =========================================================
    private fun showEditForm(book: BookLocal) {
        val bundle = Bundle().apply {
            putString("bookId", book.id)
            putString("title", book.title)
            putString("author", book.author)      // <--- KIRIM DATA ASLI
            putString("category", book.category)
            putInt("stock", book.stock)
            putString("imageUrl", book.imageUrl)
            putString("synopsis", book.synopsis)  // <--- KIRIM DATA ASLI
        }

        // Navigasi ke AddBookFragment dengan membawa data
        try {
            findNavController().navigate(R.id.action_booksFragment_to_addBookFragment, bundle)
        } catch (e: Exception) {
            try {
                findNavController().navigate(R.id.action_booksFragment_to_addBookFragment, bundle)
            } catch (e2: Exception) {
                findNavController().navigate(R.id.addBookFragment, bundle)
            }
        }
    }

    // =========================================================
    // DELETE BOOK
    // =========================================================
    private fun confirmDelete(book: BookLocal) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Buku")
            .setMessage("Yakin ingin menghapus '${book.title}'?")
            .setPositiveButton("Ya, Hapus") { _, _ ->
                db.document(book.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Buku berhasil dihapus", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // =========================================================
    // 3. MODEL LOKAL (PERBAIKAN: TAMBAH FIELD BARU)
    // =========================================================
    data class BookLocal(
        val id: String = "",
        val title: String = "",
        val author: String = "",   // <--- WAJIB ADA
        val category: String = "",
        val synopsis: String = "", // <--- WAJIB ADA
        val imageUrl: String = "",
        val stock: Int = 0
    )
}