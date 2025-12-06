package com.example.luma

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.luma.viewmodels.UserViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Auto Login Check
        if (sharedPref.getBoolean("isLoggedIn", false)) {
            val role = sharedPref.getString("role", "member")
            if (role == "admin") {
                startActivity(Intent(this, AdminMainActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }

        // Note: ID di XML kamu 'username', tapi di Firebase kita pakai Email.
        // Sebaiknya ganti hint di XML jadi "Email", atau biarkan user input email di kolom username.
        val etEmail = findViewById<EditText>(R.id.username)
        val etPassword = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login)
        val signupButton = findViewById<Button>(R.id.btnSignup)
        val loading = findViewById<ProgressBar>(R.id.loading)

        loginButton.setOnClickListener {
            val emailInput = etEmail.text.toString().trim()
            val passInput = etPassword.text.toString().trim()

            if (emailInput.isEmpty() || passInput.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loading.visibility = View.VISIBLE

            // Admin Hardcoded (Bypass Firebase)
            if (emailInput == "admin" && passInput == "admin123") {
                loading.visibility = View.GONE
                saveSession("Admin", "admin", "admin_id_001")
                startActivity(Intent(this, AdminMainActivity::class.java))
                finish()
                return@setOnClickListener
            }

            // Login Firebase
            userViewModel.login(emailInput, passInput)
        }

        // --- OBSERVASI HASIL LOGIN ---
        userViewModel.userResult.observe(this) { user ->
            loading.visibility = View.GONE
            if (user != null) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                // Simpan Sesi (UID String)
                saveSession(user.username, user.role, user.id)

                if (user.role == "admin") {
                    startActivity(Intent(this, AdminMainActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            }
        }

        userViewModel.errorMsg.observe(this) { error ->
            if (error != null) {
                loading.visibility = View.GONE
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }

        signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun saveSession(username: String, role: String, userId: String) {
        val editor = sharedPref.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.putString("username", username) // Nama user
        editor.putString("role", role)
        editor.putString("userId", userId) // Simpan UID Firebase (String)
        editor.apply()
    }
}