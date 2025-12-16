package com.example.luma

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide // Pastikan sudah add library Glide
import com.example.luma.database.Book
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddBookFragment : Fragment() {

    private lateinit var etTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var etStock: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var btnChooseImage: Button
    private lateinit var ivCoverPreview: ImageView
    private lateinit var spinnerCategory: Spinner
    private lateinit var progressBar: ProgressBar
    private lateinit var tvHeader: TextView // Tambahan buat ganti judul "Tambah" jadi "Edit"

    // Variabel untuk menyimpan Gambar yang dipilih (BARU)
    private var selectedImageUri: Uri? = null

    // --- VARIABEL MODE EDIT ---
    private var isEditMode = false
    private var editBookId: String = ""
    private var oldImageUrl: String = ""
    // --------------------------

    // Firebase
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Launcher untuk buka Galeri
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            ivCoverPreview.setImageURI(uri)
            btnChooseImage.text = ""
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_book, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Init View
        // Pastikan di XML kamu ada TextView judul dengan ID tvHeader (atau sesuaikan)
        // Kalau belum ada ID-nya di XML, tambahkan android:id="@+id/tvHeader" di TextView "Tambah Buku Baru"
        tvHeader = view.findViewById(R.id.tvHeader)

        etTitle = view.findViewById(R.id.etTitle)
        etAuthor = view.findViewById(R.id.etAuthor)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        etStock = view.findViewById(R.id.etStock)
        etDescription = view.findViewById(R.id.etDescription)

        ivCoverPreview = view.findViewById(R.id.ivCoverPreview)
        btnChooseImage = view.findViewById(R.id.btnChooseImage)

        btnSave = view.findViewById(R.id.btnSaveBook)
        btnCancel = view.findViewById(R.id.btnCancelBook)
        progressBar = view.findViewById(R.id.progressBar)

        setupSpinner()

        // --- CEK APAKAH INI MODE EDIT? ---
        if (arguments != null && requireArguments().containsKey("bookId")) {
            isEditMode = true
            editBookId = requireArguments().getString("bookId") ?: ""
            oldImageUrl = requireArguments().getString("imageUrl") ?: ""

            // 1. Ubah Judul & Tombol
            tvHeader.text = "Edit Buku"
            btnSave.text = "Update Buku"

            // 2. Isi Form dengan Data Lama
            etTitle.setText(requireArguments().getString("title"))
            etAuthor.setText(requireArguments().getString("author"))
            etStock.setText(requireArguments().getInt("stock").toString())
            etDescription.setText(requireArguments().getString("synopsis"))

            // 3. Set Spinner Category
            val oldCategory = requireArguments().getString("category")
            val adapter = spinnerCategory.adapter as ArrayAdapter<String>
            val position = adapter.getPosition(oldCategory)
            if (position >= 0) spinnerCategory.setSelection(position)

            // 4. Tampilkan Gambar Lama
            if (oldImageUrl.isNotEmpty()) {
                Glide.with(this).load(oldImageUrl).into(ivCoverPreview)
                btnChooseImage.text = "" // Hilangkan teks biar gambar kelihatan
            }
        }
        // ---------------------------------

        // Logic Pilih Gambar
        btnChooseImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Logic Simpan
        btnSave.setOnClickListener {
            if (validateFields()) {
                if (isEditMode) {
                    updateBookLogic() // Kalau Edit, update
                } else {
                    uploadImageAndSaveBook() // Kalau Baru, tambah
                }
            }
        }

        btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupSpinner() {
        val categories = arrayOf(
            "Fiksi", "Novel", "Komik", "Pendidikan", "Sejarah", "Teknologi", "Bisnis", "Lainnya"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    // --- LOGIKA TAMBAH BUKU BARU ---
    private fun uploadImageAndSaveBook() {
        val title = etTitle.text.toString().trim()
        setLoading(true)

        if (selectedImageUri != null) {
            val fileName = "covers/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(fileName)

            ref.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        saveToFirestore(uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(context, "Gagal upload gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveToFirestore("")
        }
    }

    private fun saveToFirestore(imageUrl: String) {
        val newBook = Book(
            id = "",
            title = etTitle.text.toString().trim(),
            author = etAuthor.text.toString().trim(),
            category = spinnerCategory.selectedItem.toString(),
            synopsis = etDescription.text.toString().trim(),
            stock = etStock.text.toString().toIntOrNull() ?: 0,
            rating = 0.0,
            imagePath = imageUrl
        )

        db.collection("books").add(newBook)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(context, "Buku berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(context, "Gagal simpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- LOGIKA UPDATE BUKU (EDIT) ---
    private fun updateBookLogic() {
        setLoading(true)

        // Skenario 1: User ganti gambar (Ada selectedImageUri baru)
        if (selectedImageUri != null) {
            val fileName = "covers/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(fileName)

            ref.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        // Update data dengan URL gambar BARU
                        updateFirestoreData(uri.toString())
                    }
                }
                .addOnFailureListener {
                    setLoading(false)
                    Toast.makeText(context, "Gagal upload gambar baru", Toast.LENGTH_SHORT).show()
                }
        }
        // Skenario 2: User TIDAK ganti gambar (Pakai oldImageUrl)
        else {
            updateFirestoreData(oldImageUrl)
        }
    }

    private fun updateFirestoreData(imageUrl: String) {
        val updateMap = mapOf(
            "title" to etTitle.text.toString().trim(),
            "author" to etAuthor.text.toString().trim(),
            "category" to spinnerCategory.selectedItem.toString(),
            "synopsis" to etDescription.text.toString().trim(),
            "stock" to (etStock.text.toString().toIntOrNull() ?: 0),
            "imagePath" to imageUrl
        )

        db.collection("books").document(editBookId)
            .update(updateMap)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(context, "Buku berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(context, "Gagal update: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            btnSave.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            btnSave.isEnabled = true
        }
    }

    private fun validateFields(): Boolean {
        if (TextUtils.isEmpty(etTitle.text.toString().trim())) return errorMsg(etTitle, "Judul wajib diisi")
        if (TextUtils.isEmpty(etAuthor.text.toString().trim())) return errorMsg(etAuthor, "Penulis wajib diisi")
        if (TextUtils.isEmpty(etStock.text.toString().trim())) return errorMsg(etStock, "Stok wajib diisi")

        // Validasi Gambar:
        // Kalau Mode Tambah -> Wajib ada selectedImageUri
        // Kalau Mode Edit -> Boleh null (artinya pakai gambar lama), TAPI kalau gambar lama juga kosong, wajib isi.
        if (!isEditMode && selectedImageUri == null) {
            Toast.makeText(context, "Mohon pilih cover buku!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun errorMsg(et: EditText, msg: String): Boolean {
        et.error = msg
        return false
    }
}