package com.example.luma

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    // 1. Siapkan Launcher untuk menangkap hasil izin (Diterima/Ditolak)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Izin diberikan, notifikasi aman
        } else {
            // Izin ditolak, beri tahu user secara halus (Opsional)
            Toast.makeText(this, "Notifikasi dimatikan, pengingat buku tidak akan muncul.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ==========================================
        // BAGIAN NAVIGASI (KODE LAMA KAMU)
        // ==========================================
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)

        val navController = navHostFragment?.findNavController()
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (navController != null) {
            bottomNav.setupWithNavController(navController)

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

        // ==========================================
        // BAGIAN PERMISSION NOTIFIKASI (BARU)
        // ==========================================
        askNotificationPermission()
    }

    // Fungsi untuk cek versi Android dan minta izin
    private fun askNotificationPermission() {
        // Hanya perlu untuk Android 13 (Tiramisu) ke atas (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Izin sudah ada, aman.
            } else {
                // Belum ada izin, tampilkan pop-up permintaan
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}