package com.example.luma

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // pastiin layout ini yang dipake

        // ambil navHostFragment dengan aman
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)

        val navController = navHostFragment?.findNavController()
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (navController != null) {
            bottomNav.setupWithNavController(navController)

            // biar pas klik home dari fragment lain -> balik ke HomeFragment
            bottomNav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.homeFragment -> {
                        navController.popBackStack(R.id.homeFragment, false)
                        true
                    }
                    else -> {
                        navController.navigate(item.itemId)
                        true
                    }
                }
            }
        } else {
            throw IllegalStateException("NavController tidak ditemukan di nav_host_fragment!")
        }
    }
}
