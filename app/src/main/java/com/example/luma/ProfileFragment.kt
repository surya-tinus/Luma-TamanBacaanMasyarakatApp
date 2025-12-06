package com.example.luma

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.luma.database.User
import com.example.luma.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvBirthdate: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnLogout: Button

    private lateinit var userViewModel: UserViewModel
    private lateinit var sharedPref: SharedPreferences

    // Simpan data user sementara buat dikirim ke Edit
    private var currentUser: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi ViewModel & Prefs
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // 2. Hubungkan View
        tvName = view.findViewById(R.id.tvUserName)
        tvEmail = view.findViewById(R.id.tvUserEmail)
        tvPhone = view.findViewById(R.id.tvUserPhone)
        tvAddress = view.findViewById(R.id.tvUserAddress)
        tvBirthdate = view.findViewById(R.id.tvUserBirthdate)
        btnEdit = view.findViewById(R.id.btnEditProfile)
        btnLogout = view.findViewById(R.id.btnLogout)

        // 3. Ambil UID dari Sesi & Minta Data ke Firebase
        val uid = sharedPref.getString("userId", null)
        if (uid != null) {
            userViewModel.fetchUserProfile(uid)
        } else {
            // Kalau sesi hilang, paksa logout
            logoutUser()
        }

        // 4. Observasi Data Profil (Realtime)
        userViewModel.userResult.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentUser = user
                displayData(user)
            }
        }

        // 5. Tombol Edit (Kirim data real ke EditFragment)
        btnEdit.setOnClickListener {
            // Pastikan kamu punya 'action_profileFragment_to_editProfileFragment' di nav_graph
            // Dan EditProfileFragment siap menerima Bundle ini
            if (currentUser != null) {
                val bundle = Bundle().apply {
                    putString("uid", currentUser!!.id)
                    putString("name", currentUser!!.username)
                    putString("email", currentUser!!.email)
                    putString("phone", currentUser!!.phone)
                    putString("address", currentUser!!.address)
                    putString("birthdate", currentUser!!.birthdate)
                }
                // Uncomment baris ini kalau EditProfileFragment sudah ada di nav_graph
                // findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment, bundle)
                Toast.makeText(context, "Fitur Edit Profil Segera Hadir!", Toast.LENGTH_SHORT).show()
            }
        }

        // 6. Tombol Logout
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun displayData(user: User) {
        tvName.text = user.username
        tvEmail.text = user.email
        tvPhone.text = formatPhoneNumber(user.phone)
        tvAddress.text = user.address
        tvBirthdate.text = user.birthdate
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari akun Anda?")
            .setPositiveButton("Ya") { _, _ ->
                logoutUser()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun logoutUser() {
        // 1. Logout dari Firebase
        FirebaseAuth.getInstance().signOut()

        // 2. Hapus Sesi Lokal (SharedPreferences)
        val editor = sharedPref.edit()
        editor.clear() // Hapus semua data (username, userId, isLoggedIn)
        editor.apply()

        // 3. Pindah ke Login & Hapus History
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun formatPhoneNumber(phone: String): String {
        // Logika formatting kamu tetap dipertahankan
        val digits = phone.filter { it.isDigit() }
        val parts = mutableListOf<String>()
        var i = 0
        while (i < digits.length) {
            val end = (i + 4).coerceAtMost(digits.length)
            parts.add(digits.substring(i, end))
            i += 4
        }
        return parts.joinToString("-")
    }
}