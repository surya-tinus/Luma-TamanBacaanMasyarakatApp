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
import com.example.luma.database.Book
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts

class ManageBooksFragment : Fragment() {
    private var selectedImageUri: Uri? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookAdminAdapter
    private lateinit var addButton: Button

    private val books = mutableListOf<BookLocal>()
    private val db = FirebaseFirestore.getInstance().collection("books")

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                Toast.makeText(requireContext(), "Image selected", Toast.LENGTH_SHORT).show()
            }
        }
    private fun getBookCategories(): Array<String> {
        return arrayOf(
            "Choose category",

            // Fiksi
            "Fiksi","Novel","Cerpen","Puisi","Drama","Fantasi","Fiksi Ilmiah",
            "Horor","Misteri","Thriller","Romantis","Petualangan",

            // Non-Fiksi
            "Non-Fiksi","Biografi","Autobiografi","Memoar","Esai","Jurnal",

            // Pendidikan
            "Pendidikan","Buku Pelajaran","Referensi","Kamus","Ensiklopedia",
            "Akademik","Ilmiah",

            // Anak
            "Anak","Remaja","Dongeng","Komik","Buku Bergambar",

            // Sosial
            "Sejarah","Budaya","Sosiologi","Psikologi","Filsafat","Agama","Politik",

            // Teknologi
            "Teknologi","Komputer","Pemrograman","Sains","Matematika",
            "Fisika","Biologi","Kimia",

            // Bisnis
            "Ekonomi","Bisnis","Manajemen","Keuangan","Kewirausahaan",
            "Pengembangan Diri","Motivasi",

            // Lainnya
            "Kesehatan","Olahraga","Kuliner","Travel","Seni","Musik",
            "Fotografi","Hukum","Lingkungan","Pertanian","Teknik","Umum"
        )
    }

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
                                imageUrl = book.imagePath,
                                stock = book.stock
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
            putInt("stock", book.stock)
            putString("description", "No Description")
        }
        findNavController().navigate(R.id.action_booksFragment_to_bookDetailFragment, bundle)
    }

    // =========================================================
    // ADD BOOK
    // =========================================================
    private fun showAddForm() {
        selectedImageUri = null
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_book, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etAuthor = dialogView.findViewById<EditText>(R.id.etAuthor)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)

        val categories = getBookCategories()

        val adapterCategory = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0 // disable "Choose category"
            }
        }

        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapterCategory

        val btnChooseImage = dialogView.findViewById<Button>(R.id.btnChooseImage)
        btnChooseImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Book")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTitle.text.toString()
                val author = etAuthor.text.toString()
                val category = spinnerCategory.selectedItem.toString()

                if (
                    title.isEmpty() ||
                    author.isEmpty() ||
                    spinnerCategory.selectedItemPosition == 0
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Please fill all fields and choose category",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                if (selectedImageUri == null) {
                    Toast.makeText(
                        requireContext(),
                        "Please choose a cover image",
                        Toast.LENGTH_SHORT
                    ).show()
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
                    imagePath = ""
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
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)

        val categories = getBookCategories()

        val adapterCategory = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapterCategory

        etTitle.setText(book.title)
        etAuthor.setText("")
        val index = categories.indexOf(book.category)
        if (index >= 0) spinnerCategory.setSelection(index)
        else spinnerCategory.setSelection(0)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Book")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                db.document(book.id).update(
                    mapOf(
                        "title" to etTitle.text.toString(),
                        "category" to spinnerCategory.selectedItem.toString(),
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
        val imageUrl: String = "",
        val stock: Int = 0
    )
}
