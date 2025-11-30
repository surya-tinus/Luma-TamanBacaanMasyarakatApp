package com.example.luma

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.luma.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.navigation.fragment.findNavController

class ManageBooksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookAdapter
    private lateinit var addButton: Button
    private val books = mutableListOf<BookLocal>()

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
        adapter = BookAdapter(books) { book ->
            // navigasi ke BookDetailFragment
            val bundle = Bundle().apply {
                putString("title", book.title)
                putString("category", book.category)
                putString("imageUrl", book.imageUrl)
                putString("description", "Loading description...") // placeholder, biar tidak null
            }

            // Gunakan Navigation Component
            findNavController().navigate(R.id.action_booksFragment_to_bookDetailFragment, bundle)
        }

        recyclerView.adapter = adapter

        addButton.setOnClickListener {
            showAddBookForm()
        }

        fetchBooks()
    }

    private fun showAddBookForm() {
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_add_book, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etAuthor = dialogView.findViewById<EditText>(R.id.etAuthor)
        val etCategory = dialogView.findViewById<EditText>(R.id.etCategory)
        val etCover = dialogView.findViewById<EditText>(R.id.etCoverUrl)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Book")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTitle.text.toString().trim()
                val author = etAuthor.text.toString().trim()
                val category = etCategory.text.toString().trim()
                val cover = etCover.text.toString().trim()

                if (title.isEmpty() || author.isEmpty() || category.isEmpty()) {
                    Toast.makeText(requireContext(), "All fields must be filled", Toast.LENGTH_SHORT).show()
                } else {
                    books.add(0, BookLocal(title, category, cover))
                    adapter.notifyItemInserted(0)
                    recyclerView.scrollToPosition(0)
                    Toast.makeText(requireContext(), "Book added!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun fetchBooks() {
        val categories = listOf("fiction", "technology", "science", "romance")

        categories.forEach { category ->
            RetrofitClient.instance.searchBooks("subject:$category")
                .enqueue(object : Callback<com.example.luma.model.BookResponse> {
                    override fun onResponse(
                        call: Call<com.example.luma.model.BookResponse>,
                        response: Response<com.example.luma.model.BookResponse>
                    ) {
                        if (!response.isSuccessful) return

                        val body = response.body() ?: return
                        // jika items nullable -> ganti dengan emptyList() untuk menghindari error iterator pada nullable
                        val items = body.items ?: emptyList()

                        for (item in items) {
                            val info = item.volumeInfo
                            val title = info.title ?: "Untitled"
                            val cat = info.categories?.firstOrNull() ?: category
                            val image = info.imageLinks?.thumbnail?.replace("http://", "https://") ?: ""
                            books.add(BookLocal(title, cat, image))
                        }
                        // update UI di main thread (Retrofit callback sudah di main thread, tapi aman memanggil adapter)
                        adapter.notifyDataSetChanged()
                    }

                    override fun onFailure(call: Call<com.example.luma.model.BookResponse>, t: Throwable) {
                        t.printStackTrace()
                        Toast.makeText(requireContext(), "Failed to load books: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    data class BookLocal(val title: String, val category: String, val imageUrl: String)

    class BookAdapter(
        private val list: List<BookLocal>,
        private val onClick: (BookLocal) -> Unit
    ) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

        inner class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivCover: ImageView = view.findViewById(R.id.iv_book_cover)
            val tvTitle: TextView = view.findViewById(R.id.tv_book_title)
            val tvCategory: TextView = view.findViewById(R.id.tv_book_category)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
            return BookViewHolder(v)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
            val b = list[position]
            holder.tvTitle.text = b.title
            holder.tvCategory.text = b.category
            Glide.with(holder.itemView.context)
                .load(b.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivCover)

            holder.itemView.setOnClickListener { onClick(b) }
        }
    }
}