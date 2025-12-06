package com.example.luma

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.luma.database.User
import com.example.luma.viewmodels.UserViewModel
import java.util.*

class SignupActivity : AppCompatActivity() {

    private lateinit var birthdate: EditText
    private lateinit var userViewModel: UserViewModel
    private lateinit var loadingBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Init ViewModel
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        val username = findViewById<EditText>(R.id.etSignupUsername)
        val email = findViewById<EditText>(R.id.etSignupEmail)
        val password = findViewById<EditText>(R.id.etSignupPassword)
        val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val phone = findViewById<EditText>(R.id.etSignupPhone)
        val address = findViewById<EditText>(R.id.etSignupAddress)
        birthdate = findViewById(R.id.etSignupBirthdate)

        val signupButton = findViewById<Button>(R.id.btnSignupConfirm)
        val loginRedirect = findViewById<TextView>(R.id.tvLoginRedirect)

        // Tambahkan ProgressBar di XML kamu kalau belum ada, atau pakai Toast loading
        // Untuk sekarang kita asumsikan kamu bisa tambah ProgressBar id: pbLoading di activity_signup.xml
        // Kalau ga ada, kita skip visual loading bar-nya.

        birthdate.setOnClickListener { showDatePicker() }

        signupButton.setOnClickListener {
            val user = username.text.toString().trim()
            val userEmail = email.text.toString().trim()
            val pass = password.text.toString().trim()
            val confirm = confirmPassword.text.toString().trim()
            val userPhone = phone.text.toString().trim()
            val userAddress = address.text.toString().trim()
            val userBirth = birthdate.text.toString().trim()

            if (user.isEmpty() || userEmail.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Siapkan Data User (Tanpa Password, Password dikirim terpisah ke Auth)
            val newUser = User(
                username = user,
                email = userEmail,
                phone = userPhone,
                address = userAddress,
                birthdate = userBirth,
                role = "member"
            )

            // Panggil Register Firebase
            // (ViewModel otomatis handle Auth & Firestore)
            userViewModel.register(newUser, pass)
        }

        // --- OBSERVASI HASIL REGISTER ---
        userViewModel.userResult.observe(this) { user ->
            if (user != null) {
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                userViewModel.doneNavigating()
            }
        }

        userViewModel.errorMsg.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }

        loginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d %s %d", selectedDay, getMonthName(selectedMonth), selectedYear)
                birthdate.setText(formattedDate)
            },
            year, month, day
        )
        datePicker.show()
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            0 -> "January" 1 -> "February" 2 -> "March" 3 -> "April"
            4 -> "May" 5 -> "June" 6 -> "July" 7 -> "August"
            8 -> "September" 9 -> "October" 10 -> "November" 11 -> "December"
            else -> ""
        }
    }
}