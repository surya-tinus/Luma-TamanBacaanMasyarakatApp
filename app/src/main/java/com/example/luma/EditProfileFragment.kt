package com.example.luma

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.luma.database.User
import com.example.luma.viewmodels.UserViewModel
import java.util.*

class EditProfileFragment : Fragment() {

    private lateinit var nameField: EditText
    private lateinit var emailField: EditText
    private lateinit var phoneField: EditText
    private lateinit var addressField: EditText
    private lateinit var birthdateField: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private lateinit var userViewModel: UserViewModel
    private var userId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

        // Hubungkan View (Pastikan ID Password TIDAK ADA DI SINI)
        nameField = view.findViewById(R.id.editTextName)
        emailField = view.findViewById(R.id.editTextEmail)
        phoneField = view.findViewById(R.id.editTextPhone)
        addressField = view.findViewById(R.id.editTextAddress)
        birthdateField = view.findViewById(R.id.editTextBirthdate)

        saveButton = view.findViewById(R.id.buttonSave)
        cancelButton = view.findViewById(R.id.buttonCancel)

        // Ambil Data
        arguments?.let { bundle ->
            userId = bundle.getString("uid", "")
            nameField.setText(bundle.getString("name", ""))
            emailField.setText(bundle.getString("email", ""))
            phoneField.setText(bundle.getString("phone", ""))
            addressField.setText(bundle.getString("address", ""))
            birthdateField.setText(bundle.getString("birthdate", ""))

            // Disable Email (Tidak bisa diedit di sini)
            emailField.isEnabled = false
        }

        // Date Picker
        birthdateField.setOnClickListener {
            showDatePickerDialog(birthdateField.text.toString())
        }

        // Tombol Save (Hanya update Biodata, BUKAN Password)
        saveButton.setOnClickListener {
            if (validateInputs()) {
                val updatedUser = User(
                    id = userId,
                    username = nameField.text.toString().trim(),
                    email = emailField.text.toString().trim(),
                    phone = phoneField.text.toString().trim(),
                    address = addressField.text.toString().trim(),
                    birthdate = birthdateField.text.toString().trim(),
                    role = "member"
                )

                // Update ke Firestore
                userViewModel.updateUser(updatedUser)

                Toast.makeText(context, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        // Tombol Cancel
        cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showDatePickerDialog(currentDate: String) {
        val calendar = Calendar.getInstance()
        if (currentDate.isNotEmpty()) {
            val dateParts = currentDate.split("/")
            if (dateParts.size == 3) {
                try {
                    calendar.set(dateParts[2].toInt(), dateParts[1].toInt() - 1, dateParts[0].toInt())
                } catch (_: Exception) { }
            }
        }

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val formatted = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                birthdateField.setText(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val email = emailField.text.toString().trim()
        val phone = phoneField.text.toString().trim()
        val birthdate = birthdateField.text.toString().trim()
        val address = addressField.text.toString().trim()

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.error = "Invalid email format"
            isValid = false
        }

        if (phone.isEmpty() || !phone.matches(Regex("^[0-9]{10,13}$"))) {
            phoneField.error = "Phone must be 10–13 digits"
            isValid = false
        }

        if (birthdate.isEmpty()) {
            birthdateField.error = "Please select your birthdate"
            isValid = false
        }

        if (address.isEmpty()) {
            addressField.error = "Address cannot be empty"
            isValid = false
        }

        if (nameField.text.isNullOrEmpty()) {
            nameField.error = "Name cannot be empty"
            isValid = false
        }

        // Tidak ada validasi password di sini karena field-nya sudah dihapus

        return isValid
    }
}