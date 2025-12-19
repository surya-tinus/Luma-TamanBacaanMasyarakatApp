package com.example.luma

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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
    private lateinit var tvHeader: TextView

    // Variabel Gambar
    private var selectedImageUri: Uri? = null

    // --- VARIABEL MODE EDIT ---
    private var isEditMode = false
    private var editBookId: String = ""
    private var oldImageUrl: String = ""
    private var initialCategoryFromEdit: String? = null // Untuk simpan kategori awal pas edit
    // --------------------------

    // Variabel Kategori Dinamis
    private val categoryList = mutableListOf<String>()
    private var selectedCategoryString: String = "" // Menyimpan kategori yang fix dipilih
    private val ADD_NEW_CATEGORY_LABEL = "+ Tambah Kategori Baru" // Label pemicu

    // Firebase
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

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
        super.onViewCreated(view, savedInstanceState)

        // 1. INIT VIEW
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

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { findNavController().navigateUp() }

        // 2. SETUP SPINNER & LOAD DATA
        fetchCategoriesAndSetupSpinner()

        // 3. CEK MODE EDIT
        if (arguments != null && requireArguments().containsKey("bookId")) {
            isEditMode = true
            editBookId = requireArguments().getString("bookId") ?: ""
            oldImageUrl = requireArguments().getString("imageUrl") ?: ""
            initialCategoryFromEdit = requireArguments().getString("category")

            tvHeader.text = "Edit Buku"
            btnSave.text = "Update Buku"

            etTitle.setText(requireArguments().getString("title"))
            etAuthor.setText(requireArguments().getString("author"))
            etStock.setText(requireArguments().getInt("stock").toString())
            etDescription.setText(requireArguments().getString("synopsis"))

            if (oldImageUrl.isNotEmpty()) {
                Glide.with(this).load(oldImageUrl).into(ivCoverPreview)
                btnChooseImage.text = ""
            }
        }

        // 4. LISTENERS
        btnChooseImage.setOnClickListener { pickImageLauncher.launch("image/*") }

        btnSave.setOnClickListener {
            if (validateFields()) {
                if (isEditMode) updateBookLogic() else uploadImageAndSaveBook()
            }
        }

        btnCancel.setOnClickListener { findNavController().navigateUp() }
    }

    // =========================================================================
    // LOGIKA KATEGORI DINAMIS (BACA INI)
    // =========================================================================

    private fun fetchCategoriesAndSetupSpinner() {
        // Ambil data real-time dari Firestore
        db.collection("categories")
            .orderBy("name")
            .get()
            .addOnSuccessListener { result ->
                categoryList.clear()

                // Masukkan hasil dari DB
                for (document in result) {
                    val name = document.getString("name")
                    if (name != null) categoryList.add(name)
                }

                // Tambahkan menu spesial di paling bawah
                categoryList.add(ADD_NEW_CATEGORY_LABEL)

                setupSpinnerAdapter()
            }
            .addOnFailureListener {
                // Fallback kalau offline/gagal, tetap kasih opsi tambah
                categoryList.clear()
                categoryList.add("Fiksi") // Default minimal
                categoryList.add(ADD_NEW_CATEGORY_LABEL)
                setupSpinnerAdapter()
            }
    }

    private fun setupSpinnerAdapter() {
        if (context == null) return

        // Gunakan layout custom yang kita buat tadi
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner_selected, // Layout saat tertutup
            categoryList
        )
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown) // Layout saat dibuka

        spinnerCategory.adapter = adapter

        // Setup Listener & Auto Select (Sama seperti sebelumnya)
        setupSpinnerListener()

        if (isEditMode && initialCategoryFromEdit != null) {
            val position = adapter.getPosition(initialCategoryFromEdit)
            if (position >= 0) {
                spinnerCategory.setSelection(position)
                selectedCategoryString = initialCategoryFromEdit!!
            }
        } else if (selectedCategoryString.isNotEmpty()) {
            val position = adapter.getPosition(selectedCategoryString)
            if (position >= 0) spinnerCategory.setSelection(position)
        }
    }

    private fun setupSpinnerListener() {
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = categoryList[position]

                if (selectedItem == ADD_NEW_CATEGORY_LABEL) {
                    // JIKA KLIK "+ TAMBAH BARU", MUNCULKAN DIALOG
                    showAddCategoryDialog()
                } else {
                    // Jika kategori biasa, simpan ke variabel
                    selectedCategoryString = selectedItem
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showAddCategoryDialog() {
        // 1. Inflate Layout Custom
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category, null)

        // 2. Init View di dalam Dialog
        val etNewCategory = dialogView.findViewById<EditText>(R.id.etNewCategory)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancelDialog)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveDialog)

        // 3. Buat Alert Dialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()

        // Supaya background dialog transparan (biar rounded corner CardView kelihatan)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 4. Logic Tombol
        btnCancel.setOnClickListener {
            dialog.dismiss()
            spinnerCategory.setSelection(0) // Reset ke item pertama
        }

        btnSave.setOnClickListener {
            val newCategoryName = etNewCategory.text.toString().trim()
            if (newCategoryName.isNotEmpty()) {
                saveNewCategoryToFirestore(newCategoryName)
                dialog.dismiss()
            } else {
                etNewCategory.error = "Nama kategori tidak boleh kosong"
            }
        }

        dialog.show()
    }

    private fun saveNewCategoryToFirestore(categoryName: String) {
        setLoading(true)
        val categoryMap = hashMapOf("name" to categoryName)

        db.collection("categories")
            .add(categoryMap)
            .addOnSuccessListener {
                Toast.makeText(context, "Kategori '$categoryName' ditambahkan!", Toast.LENGTH_SHORT).show()
                setLoading(false)

                // Update variabel biar pas refresh langsung kepilih
                selectedCategoryString = categoryName
                // Kalau mode edit, update juga ini biar ga ketimpa
                if (isEditMode) initialCategoryFromEdit = categoryName

                // Refresh Spinner
                fetchCategoriesAndSetupSpinner()
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(context, "Gagal menambah kategori", Toast.LENGTH_SHORT).show()
                spinnerCategory.setSelection(0)
            }
    }

    // =========================================================================
    // LOGIKA SIMPAN BUKU
    // =========================================================================

    private fun uploadImageAndSaveBook() {
        setLoading(true)
        if (selectedImageUri != null) {
            val fileName = "covers/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(fileName)
            ref.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri -> saveToFirestore(uri.toString()) }
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(context, "Gagal upload: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveToFirestore("")
        }
    }

    private fun saveToFirestore(imageUrl: String) {
        // PENTING: Gunakan 'selectedCategoryString', BUKAN 'spinnerCategory.selectedItem'
        // Karena spinner bisa saja sedang di posisi "+ Tambah Baru" saat proses simpan

        val newBook = Book(
            id = "",
            title = etTitle.text.toString().trim(),
            author = etAuthor.text.toString().trim(),
            category = selectedCategoryString, // <--- PAKE INI
            synopsis = etDescription.text.toString().trim(),
            stock = etStock.text.toString().toIntOrNull() ?: 0,
            rating = 0.0,
            imagePath = imageUrl
        )

        db.collection("books").add(newBook)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(context, "Berhasil!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(context, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateBookLogic() {
        setLoading(true)
        if (selectedImageUri != null) {
            val fileName = "covers/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(fileName)
            ref.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri -> updateFirestoreData(uri.toString()) }
                }
                .addOnFailureListener {
                    setLoading(false)
                    Toast.makeText(context, "Gagal upload", Toast.LENGTH_SHORT).show()
                }
        } else {
            updateFirestoreData(oldImageUrl)
        }
    }

    private fun updateFirestoreData(imageUrl: String) {
        val updateMap = mapOf(
            "title" to etTitle.text.toString().trim(),
            "author" to etAuthor.text.toString().trim(),
            "category" to selectedCategoryString, // <--- PAKE INI
            "synopsis" to etDescription.text.toString().trim(),
            "stock" to (etStock.text.toString().toIntOrNull() ?: 0),
            "imagePath" to imageUrl
        )

        db.collection("books").document(editBookId)
            .update(updateMap)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(context, "Updated!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(context, "Gagal update", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            btnSave.isEnabled = false
            btnCancel.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            btnSave.isEnabled = true
            btnCancel.isEnabled = true
        }
    }

    private fun validateFields(): Boolean {
        if (TextUtils.isEmpty(etTitle.text.toString().trim())) return errorMsg(etTitle, "Isi Judul")
        if (TextUtils.isEmpty(etAuthor.text.toString().trim())) return errorMsg(etAuthor, "Isi Penulis")
        if (TextUtils.isEmpty(etStock.text.toString().trim())) return errorMsg(etStock, "Isi Stok")

        // Validasi Kategori: Jangan sampai user submit pas pilih "+ Tambah Baru" tapi ga jadi nambah
        if (selectedCategoryString.isEmpty() || selectedCategoryString == ADD_NEW_CATEGORY_LABEL) {
            Toast.makeText(context, "Pilih kategori yang valid!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!isEditMode && selectedImageUri == null) {
            Toast.makeText(context, "Pilih Cover!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun errorMsg(et: EditText, msg: String): Boolean {
        et.error = msg
        return false
    }
}