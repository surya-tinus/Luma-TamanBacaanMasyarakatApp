package com.example.luma

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)

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

            loginButton.postDelayed({
                loading.visibility = View.GONE

                // Admin / User default
                when {
                    user == "admin" && pass == "admin123" -> {
                        startActivity(Intent(this, AdminMainActivity::class.java))
                    }
                    user == "user" && pass == "user123" -> {
                        startActivity(Intent(this, MainActivity::class.java))
                    }

                    // Akun yang disimpan
                    else -> {
                        val savedUser = sharedPref.getString("username", "")
                        val savedPass = sharedPref.getString("password", "")

                        if (user == savedUser && pass == savedPass) {
                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            Toast.makeText(this, "Invalid credentials!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }, 1000)
        }

        signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}