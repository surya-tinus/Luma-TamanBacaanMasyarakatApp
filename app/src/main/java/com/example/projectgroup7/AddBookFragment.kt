package com.example.projectgroup7

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.projectgroup7.model.Book

class AddBookFragment : Fragment() {

    private lateinit var etTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var etCategory: EditText
    private lateinit var etStock: EditText
    private lateinit var etCover: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_book, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        etTitle = view.findViewById(R.id.etTitle)
        etAuthor = view.findViewById(R.id.etAuthor)
        etCategory = view.findViewById(R.id.etCategory)
        etStock = view.findViewById(R.id.etStock)
        etCover = view.findViewById(R.id.etCover)
        etDescription = view.findViewById(R.id.etDescription)
        btnSave = view.findViewById(R.id.btnSaveBook)
        btnCancel = view.findViewById(R.id.btnCancelBook)

        btnSave.setOnClickListener {
            if (validateFields()) {
                // Build Book object (stock ignored in Book model for now)
                val book = Book(
                    title = etTitle.text.toString().trim(),
                    category = etCategory.text.toString().trim(),
                    imageUrl = etCover.text.toString().trim(),
                    description = etDescription.text.toString().trim()
                )

                // send result back to AdminHomeFragment or BooksFragment via FragmentResult
                val bundle = Bundle().apply {
                    putString("title", book.title)
                    putString("category", book.category)
                    putString("imageUrl", book.imageUrl)
                    putString("description", book.description)
                }
                parentFragmentManager.setFragmentResult("book_added", bundle)
                Toast.makeText(requireContext(), "Book added successfully!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }

        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun validateFields(): Boolean {
        var ok = true
        if (TextUtils.isEmpty(etTitle.text.toString().trim())) {
            etTitle.error = "Title required"
            ok = false
        }
        if (TextUtils.isEmpty(etAuthor.text.toString().trim())) {
            etAuthor.error = "Author required"
            ok = false
        }
        if (TextUtils.isEmpty(etCategory.text.toString().trim())) {
            etCategory.error = "Category required"
            ok = false
        }
        if (TextUtils.isEmpty(etStock.text.toString().trim())) {
            etStock.error = "Stock required"
            ok = false
        } else {
            val num = etStock.text.toString().toIntOrNull()
            if (num == null || num < 0) {
                etStock.error = "Stock must be a non-negative number"
                ok = false
            }
        }
        return ok
    }
}