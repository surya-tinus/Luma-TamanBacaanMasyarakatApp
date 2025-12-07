package com.example.luma

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.luma.model.Book
import com.google.firebase.firestore.FirebaseFirestore

class ManageBooksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookAdapter
    private lateinit var addButton: Button
    private val bookList = mutableListOf<Book>()

    private val firestore = FirebaseFirestore.getInstance()

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
        adapter = BookAdapter(bookList) { book ->

            val bundle = Bundle().apply {
                putString("title", book.title)
                putString("category", book.category)
                putString("imageUrl", book.imageUrl)
                putString("description", book.description)
            }

            findNavController().navigate(
                R.id.action_booksFragment_to_bookDetailFragment,
                bundle
            )
        }

        recyclerView.adapter = adapter

        addButton.setOnClickListener { showAddBookDialog() }

        fetchBooksFromFirestore()
    }

    private fun fetchBooksFromFirestore() {
        firestore.collection("books")
            .get()
            .addOnSuccessListener { result ->
                bookList.clear()

                for (doc in result) {
                    val book = doc.toObject(Book::class.java).copy(id = doc.id)
                    bookList.add(book)
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load books", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddBookDialog() {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_book, null)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etAuthor = view.findViewById<EditText>(R.id.etAuthor)
        val etCategory = view.findViewById<EditText>(R.id.etCategory)
        val etCover = view.findViewById<EditText>(R.id.etCoverUrl)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Book")
            .setView(view)
            .setPositiveButton("Add") { _, _ ->
                val book = Book(
                    title = etTitle.text.toString(),
                    author = etAuthor.text.toString(),
                    category = etCategory.text.toString(),
                    imageUrl = etCover.text.toString(),
                    description = ""
                )

                firestore.collection("books")
                    .add(book)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Book added!", Toast.LENGTH_SHORT).show()
                        fetchBooksFromFirestore()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    class BookAdapter(
        private val list: List<Book>,
        private val onClick: (Book) -> Unit
    ) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

        inner class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivCover: ImageView = view.findViewById(R.id.iv_book_cover)
            val tvTitle: TextView = view.findViewById(R.id.tv_book_title)
            val tvCategory: TextView = view.findViewById(R.id.tv_book_category)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_book, parent, false)
            return BookViewHolder(v)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
            val book = list[position]

            holder.tvTitle.text = book.title
            holder.tvCategory.text = book.category

            Glide.with(holder.itemView.context)
                .load(book.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivCover)

            holder.itemView.setOnClickListener { onClick(book) }
        }
    }
}