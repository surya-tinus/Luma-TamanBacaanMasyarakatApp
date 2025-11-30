package com.example.luma

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.luma.database.viewmodels.UserViewModel // Pastikan import ini ada

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var userViewModel: UserViewModel // 1. Tambah ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // XML tetap pakai punya kamu

        // 2. Inisialisasi ViewModel
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Cek jika sudah login sebelumnya (Auto-login)
        if (sharedPref.getBoolean("isLoggedIn", false)) {
            val role = sharedPref.getString("role", "member")
            if (role == "admin") {
                startActivity(Intent(this, AdminMainActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login)
        val signupButton = findViewById<Button>(R.id.btnSignup)
        val loading = findViewById<ProgressBar>(R.id.loading)

        loginButton.setOnClickListener {
            val user = username.text.toString().trim()
            val pass = password.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loading.visibility = View.VISIBLE

            // Kita buat delay sedikit biar loading bar-nya kelihatan (UX)
            loginButton.postDelayed({
                // 3. Logika Login
                when {
                    // Akun Hardcoded (Admin)
                    user == "admin" && pass == "admin123" -> {
                        loading.visibility = View.GONE
                        saveSession("Admin", "admin", 0) // Simpan sesi dummy
                        startActivity(Intent(this, AdminMainActivity::class.java))
                        finish()
                    }
                    // Akun Hardcoded (User Biasa)
                    user == "user" && pass == "user123" -> {
                        loading.visibility = View.GONE
                        saveSession("User Default", "member", 0) // Simpan sesi dummy
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }

                    // Cek ke Database Room (User yang daftar lewat Signup)
                    else -> {
                        // Panggil fungsi login di ViewModel
                        userViewModel.login(user, pass)
                        // Note: Loading jangan dimatikan di sini, tapi di dalam Observer di bawah
                    }
                }
            }, 1000)
        }

        // 4. Observer untuk menangkap hasil dari Database
        userViewModel.loginResult.observe(this) { userData ->
            // Matikan loading bar setelah database merespon
            loading.visibility = View.GONE

            if (userData != null) {
                // LOGIN SUKSES via Database
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                // Simpan Sesi (PENTING: Agar MainActivity tahu siapa yang login)
                saveSession(userData.username, userData.role, userData.id)

                // Arahkan sesuai role (jika nanti dikembangkan ada role di db)
                if (userData.role == "admin") {
                    startActivity(Intent(this, AdminMainActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            } else {
                // LOGIN GAGAL (Data tidak ditemukan di database)
                // Kita cek dulu apakah input field tidak kosong agar toast tidak muncul saat inisialisasi
                if (username.text.isNotEmpty()) {
                    Toast.makeText(this, "Invalid credentials!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    // Fungsi helper untuk menyimpan sesi login
    private fun saveSession(username: String, role: String, userId: Int) {
        val editor = sharedPref.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.putString("username", username)
        editor.putString("role", role)
        editor.putInt("userId", userId)
        editor.apply()
    }
}