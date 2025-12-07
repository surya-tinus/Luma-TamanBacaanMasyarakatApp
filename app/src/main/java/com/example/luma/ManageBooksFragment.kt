package com.example.luma

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.luma.database.Book
import com.google.firebase.firestore.FirebaseFirestore

class ManageBooksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookAdminAdapter
    private lateinit var addButton: Button

    private val books = mutableListOf<BookLocal>()
    private val db = FirebaseFirestore.getInstance().collection("books")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manage_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewBooks)
        addButton = view.findViewById(R.id.btnAddBook)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = BookAdminAdapter(
            list = books,
            onClick = { book -> openDetail(book) },
            onEdit = { book -> showEditForm(book) },
            onDelete = { book -> confirmDelete(book) }
        )
        recyclerView.adapter = adapter

        addButton.setOnClickListener { showAddForm() }

        fetchBooks()
    }

    // =========================================================
    // FETCH BOOKS
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
                    val book = doc.toObject(Book::class.java)
                    if (book != null) {
                        books.add(
                            BookLocal(
                                id = doc.id,
                                title = book.title,
                                category = book.category,
                                imageUrl = book.imagePath
                            )
                        )
                    }
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    // =========================================================
    // OPEN DETAIL
    // =========================================================
    private fun openDetail(book: BookLocal) {
        val bundle = Bundle().apply {
            putString("title", book.title)
            putString("category", book.category)
            putString("imageUrl", book.imageUrl)
            putString("description", "No Description")
        }
        findNavController().navigate(R.id.action_booksFragment_to_bookDetailFragment, bundle)
    }

    // =========================================================
    // ADD BOOK
    // =========================================================
    private fun showAddForm() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_book, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etAuthor = dialogView.findViewById<EditText>(R.id.etAuthor)
        val etCategory = dialogView.findViewById<EditText>(R.id.etCategory)
        val etCover = dialogView.findViewById<EditText>(R.id.etCoverUrl)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Book")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTitle.text.toString()
                val author = etAuthor.text.toString()
                val category = etCategory.text.toString()
                val cover = etCover.text.toString()

                if (title.isEmpty() || author.isEmpty() || category.isEmpty()) {
                    Toast.makeText(requireContext(), "All fields must be filled", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newBook = Book(
                    id = "",
                    title = title,
                    author = author,
                    category = category,
                    synopsis = "",
                    stock = 5,
                    rating = 0.0,
                    imagePath = cover
                )

                db.add(newBook).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Book Added!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // =========================================================
    // EDIT BOOK
    // =========================================================
    private fun showEditForm(book: BookLocal) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_book, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etAuthor = dialogView.findViewById<EditText>(R.id.etAuthor)
        val etCategory = dialogView.findViewById<EditText>(R.id.etCategory)
        val etCover = dialogView.findViewById<EditText>(R.id.etCoverUrl)

        etTitle.setText(book.title)
        etAuthor.setText("")
        etCategory.setText(book.category)
        etCover.setText(book.imageUrl)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Book")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                db.document(book.id).update(
                    mapOf(
                        "title" to etTitle.text.toString(),
                        "category" to etCategory.text.toString(),
                        "imagePath" to etCover.text.toString()
                    )
                ).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Book Updated!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // =========================================================
    // DELETE BOOK
    // =========================================================
    private fun confirmDelete(book: BookLocal) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Book")
            .setMessage("Are you sure you want to delete '${book.title}'?")
            .setPositiveButton("Yes") { _, _ -> deleteBook(book) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteBook(book: BookLocal) {
        db.document(book.id).delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Book Deleted!", Toast.LENGTH_SHORT).show()
            }
    }

    // =========================================================
    // MODEL
    // =========================================================
    data class BookLocal(
        val id: String = "",
        val title: String = "",
        val category: String = "",
        val imageUrl: String = ""
    )
}
