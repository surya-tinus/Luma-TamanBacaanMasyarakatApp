package com.example.luma

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.luma.database.User
import com.example.luma.database.viewmodels.UserViewModel
import java.util.*

class SignupActivity : AppCompatActivity() {

    private lateinit var birthdate: EditText
    // 1. Ganti SharedPreferences dengan ViewModel
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup) // Tetap pakai layout desain kamu

        // 2. Inisialisasi ViewModel
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        val username = findViewById<EditText>(R.id.etSignupUsername)
        val email = findViewById<EditText>(R.id.etSignupEmail)
        val password = findViewById<EditText>(R.id.etSignupPassword)
        val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val phone = findViewById<EditText>(R.id.etSignupPhone)
        val address = findViewById<EditText>(R.id.etSignupAddress)
        // Note: ID etSignupBirthdate di layout kamu ada di dalam TextInputLayout,
        // tapi findViewById biasanya tetap bisa menemukannya jika ID-nya benar di TextInputEditText
        birthdate = findViewById(R.id.etSignupBirthdate)

        val signupButton = findViewById<Button>(R.id.btnSignupConfirm)
        val loginRedirect = findViewById<TextView>(R.id.tvLoginRedirect)

        // ✨ Logika DatePicker (Tetap sama seperti kodemu)
        birthdate.setOnClickListener {
            showDatePicker()
        }

        signupButton.setOnClickListener {
            val user = username.text.toString().trim()
            val userEmail = email.text.toString().trim()
            val pass = password.text.toString().trim()
            val confirm = confirmPassword.text.toString().trim()
            val userPhone = phone.text.toString().trim()
            val userAddress = address.text.toString().trim()
            val userBirth = birthdate.text.toString().trim()

            // Validasi Input Kosong
            if (user.isEmpty() || userEmail.isEmpty() || pass.isEmpty() || confirm.isEmpty() ||
                userPhone.isEmpty() || userAddress.isEmpty() || userBirth.isEmpty()
            ) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validasi Password Match
            if (pass != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Simpan ke Database Room via ViewModel
            val newUser = User(
                username = user,
                email = userEmail,
                password = pass,
                phone = userPhone,
                address = userAddress,
                birthdate = userBirth,
                role = "member"
            )

            userViewModel.register(newUser)
        }

        // 4. Observasi Status Register (Sukses/Gagal)
        userViewModel.registerStatus.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Registration Failed! Username might be taken.", Toast.LENGTH_SHORT).show()
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