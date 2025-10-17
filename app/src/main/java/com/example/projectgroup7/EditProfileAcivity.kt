package com.example.projectgroup7

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var nameField: EditText
    private lateinit var emailField: EditText
    private lateinit var phoneField: EditText
    private lateinit var birthdateField: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_edit_profile) // gunakan layout yang sama

        nameField = findViewById(R.id.editTextName)
        emailField = findViewById(R.id.editTextEmail)
        phoneField = findViewById(R.id.editTextPhone)
        birthdateField = findViewById(R.id.editTextBirthdate)
        saveButton = findViewById(R.id.buttonSave)
        cancelButton = findViewById(R.id.buttonCancel)

        // ambil data dari intent
        val name = intent.getStringExtra("name") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val phone = intent.getStringExtra("phone") ?: ""
        val birthdate = intent.getStringExtra("birthdate") ?: ""

        nameField.setText(name)
        emailField.setText(email)
        phoneField.setText(phone)
        birthdateField.setText(birthdate)

        birthdateField.setOnClickListener { showDatePickerDialog(birthdate) }

        saveButton.setOnClickListener {
            if (validateInputs()) {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                finish() // kembali ke admin profile
            }
        }

        cancelButton.setOnClickListener { finish() }
    }

    private fun showDatePickerDialog(currentDate: String) {
        val calendar = Calendar.getInstance()
        val dateParts = currentDate.split("/")
        if (dateParts.size == 3) {
            try {
                calendar.set(dateParts[2].toInt(), dateParts[1].toInt() - 1, dateParts[0].toInt())
            } catch (_: Exception) {}
        }

        val datePicker = DatePickerDialog(
            this,
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