package com.example.projectgroup7

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.*

class EditProfileFragment : Fragment() {

    private lateinit var nameField: EditText
    private lateinit var emailField: EditText
    private lateinit var phoneField: EditText
    private lateinit var birthdateField: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private var userName = ""
    private var userEmail = ""
    private var userPhone = ""
    private var userBirthdate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userName = it.getString("name", "")
            userEmail = it.getString("email", "")
            userPhone = it.getString("phone", "")
            userBirthdate = it.getString("birthdate", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        nameField = view.findViewById(R.id.editTextName)
        emailField = view.findViewById(R.id.editTextEmail)
        phoneField = view.findViewById(R.id.editTextPhone)
        birthdateField = view.findViewById(R.id.editTextBirthdate)
        saveButton = view.findViewById(R.id.buttonSave)
        cancelButton = view.findViewById(R.id.buttonCancel)

        nameField.setText(userName)
        emailField.setText(userEmail)
        phoneField.setText(userPhone)
        birthdateField.setText(userBirthdate)

        // Saat klik field birthdate → buka DatePickerDialog
        birthdateField.setOnClickListener {
            showDatePickerDialog()
        }

        saveButton.setOnClickListener {
            if (validateInputs()) {
                val bundle = Bundle().apply {
                    putString("name", nameField.text.toString())
                    putString("email", emailField.text.toString())
                    putString("phone", phoneField.text.toString())
                    putString("birthdate", birthdateField.text.toString())
                }
                parentFragmentManager.setFragmentResult("profile_update", bundle)
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    // DatePickerDialog untuk memilih tanggal
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val dateParts = userBirthdate.split("/")
        if (dateParts.size == 3) {
            try {
                calendar.set(
                    dateParts[2].toInt(),
                    dateParts[1].toInt() - 1,
                    dateParts[0].toInt()
                )
            } catch (_: Exception) { }
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

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.error = "Invalid email format"
            isValid = false
        }

        if (phone.isEmpty() || !phone.matches(Regex("^[0-9]{10,12}$"))) {
            phoneField.error = "Phone must be 10–12 digits"
            isValid = false
        }

        if (birthdate.isEmpty()) {
            birthdateField.error = "Please select your birthdate"
            isValid = false
        }

        if (nameField.text.isNullOrEmpty()) {
            nameField.error = "Name cannot be empty"
            isValid = false
        }

        return isValid
    }
}