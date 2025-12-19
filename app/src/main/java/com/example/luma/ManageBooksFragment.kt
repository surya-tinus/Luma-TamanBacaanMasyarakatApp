package com.example.luma

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView // Don't forget to import ImageView
import android.widget.TextView // Don't forget to import TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luma.database.Book
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar // Import Calendar for time

class ManageBooksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookAdminAdapter
    private lateinit var addButton: FloatingActionButton

    private val books = mutableListOf<BookLocal>()
    private val db = FirebaseFirestore.getInstance().collection("books")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_manage_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 1. SETUP GREETING (Sama kayak HomeFragment) ---
        setupGreeting(view)

        recyclerView = view.findViewById(R.id.recyclerViewBooks)
        addButton = view.findViewById(R.id.btnAddBook)

        // Gunakan GridLayoutManager (2 kolom)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = BookAdminAdapter(
            list = books,
            onClick = { book ->
                // Opsional: Buka detail buku jika diklik gambarnya
            },
            onEdit = { book -> showEditForm(book) },   // Aksi Edit
            onDelete = { book -> confirmDelete(book) } // Aksi Hapus
        )
        recyclerView.adapter = adapter

        // Logic Tombol Tambah (FAB)
        addButton.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_booksFragment_to_addBookFragment)
            } catch (e: Exception) {
                try {
                    findNavController().navigate(R.id.addBookFragment)
                } catch (e2: Exception) {
                    Toast.makeText(requireContext(), "Error Navigasi: Cek ID di nav_graph", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fetchBooks()
    }

    // --- FUNCTION GREETING ---
    private fun setupGreeting(view: View) {
        // Pastikan ID di XML fragment_manage_books kamu sudah sesuai
        // tv_greeting_admin, username_admin, iv_header_admin (Ganti ID biar gak bentrok sama Home kalau mau, atau samain aja gpp)
        // Di contoh XML sebelumnya, ID-nya belum ada. Nanti kita update XML-nya.

        // Asumsi ID di XML nanti:
        val greetingTextView = view.findViewById<TextView>(R.id.tv_greeting_admin)
        val subtitleTextView = view.findViewById<TextView>(R.id.tv_subtitle_admin)
        val headerImage = view.findViewById<ImageView>(R.id.iv_header_admin)

        // Ambil Jam Sekarang
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        // Logic Greeting sama persis kayak HomeFragment
        val (greetingText, imageResId) = when (currentHour) {
            in 5..10 -> "Selamat Pagi," to R.drawable.greetings_pagi
            in 11..14 -> "Selamat Siang," to R.drawable.greetings_siang
            in 15..17 -> "Selamat Sore," to R.drawable.greetings_sore
            else -> "Selamat Malam," to R.drawable.greetings_malam
        }

        // Set Text & Image
        greetingTextView.text = greetingText
        subtitleTextView.text = "Admin" // Statis aja atau ambil dari SharedPrefs

        try {
            headerImage.setImageResource(imageResId)
        } catch (e: Exception) {
            headerImage.setImageResource(R.drawable.library)
        }
    }

    // =========================================================
    // FETCH BOOKS (Sama)
    // =========================================================
    private fun fetchBooks() {
        db.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
                return@addSnapshotListener
            }
            if (snapshot != null) {
                books.clear()
                for (doc in snapshot.documents) {
                    val bookFirestore = doc.toObject(Book::class.java)
                    if (bookFirestore != null) {
                        books.add(
                            BookLocal(
                                id = doc.id,
                                title = bookFirestore.title ?: "",
                                author = bookFirestore.author ?: "",
                                category = bookFirestore.category ?: "",
                                synopsis = bookFirestore.synopsis ?: "",
                                imageUrl = bookFirestore.imagePath ?: "",
                                stock = bookFirestore.stock ?: 0
                            )
                        )
                    }
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    // =========================================================
    // EDIT & DELETE (Sama)
    // =========================================================
    private fun showEditForm(book: BookLocal) {
        val bundle = Bundle().apply {
            putString("bookId", book.id)
            putString("title", book.title)
            putString("author", book.author)
            putString("category", book.category)
            putInt("stock", book.stock)
            putString("imageUrl", book.imageUrl)
            putString("synopsis", book.synopsis)
        }
        try {
            findNavController().navigate(R.id.action_booksFragment_to_addBookFragment, bundle)
        } catch (e: Exception) {
            findNavController().navigate(R.id.addBookFragment, bundle)
        }
    }

    private fun confirmDelete(book: BookLocal) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Buku")
            .setMessage("Yakin ingin menghapus '${book.title}'?")
            .setPositiveButton("Ya, Hapus") { _, _ ->
                db.document(book.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Buku berhasil dihapus", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Gagal menghapus buku", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    data class BookLocal(
        val id: String = "",
        val title: String = "",
        val author: String = "",
        val category: String = "",
        val synopsis: String = "",
        val imageUrl: String = "",
        val stock: Int = 0
    )
}