package com.example.projectgroup7

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.admin_nav_host_fragment)

        val navController = navHostFragment?.findNavController()
        val bottomNav = findViewById<BottomNavigationView>(R.id.admin_bottom_navigation)

        if (navController != null) {
            bottomNav.setupWithNavController(navController)
        } else {
            throw IllegalStateException("Admin NavController tidak ditemukan!")
        }
    }
}