package com.example.luma

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminProfileFragment : Fragment(R.layout.fragment_admin_profile) {

    // View Components
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvBirthdate: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnChangePass: Button // Tambahan tombol ganti password
    private lateinit var btnLogout: Button

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi View
        tvName = view.findViewById(R.id.tvAdminName)
        tvEmail = view.findViewById(R.id.tvAdminEmail)
        tvPhone = view.findViewById(R.id.tvAdminPhone)
        tvAddress = view.findViewById(R.id.tvAdminAddress)
        tvBirthdate = view.findViewById(R.id.tvAdminBirthdate)
        btnEdit = view.findViewById(R.id.btnEditProfile)
        btnChangePass = view.findViewById(R.id.btnChangePass)
        btnLogout = view.findViewById(R.id.btnLogout)

        userId = auth.currentUser?.uid

        // Ambil data saat pertama kali dibuka
        if (userId != null) {
            fetchAdminData(userId!!)
        } else {
            // Kalau user null, lempar ke login (security check)
            forceLogout()
        }

        // Tombol Edit
        btnEdit.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            // Kirim data yang sekarang tampil ke activity edit
            intent.putExtra("name", tvName.text.toString())
            intent.putExtra("email", tvEmail.text.toString())
            intent.putExtra("phone", tvPhone.text.toString())
            intent.putExtra("address", tvAddress.text.toString())
            intent.putExtra("birthdate", tvBirthdate.text.toString())
            startActivity(intent)
        }

        // Tombol Ganti Password
        btnChangePass.setOnClickListener {
            // Opsional: Arahkan ke Activity Ganti Password atau tampilkan Dialog reset
            // Untuk simpelnya, kita bisa pakai fitur Reset Password via Email dari Firebase
            showChangePasswordDialog()
        }

        // Tombol Logout
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        // Listener jika ada perubahan data dari EditProfileActivity
        // (Opsional: Sebenarnya lebih baik fetch ulang dari database di onResume)
        parentFragmentManager.setFragmentResultListener("profile_update", viewLifecycleOwner) { _, _ ->
            if (userId != null) fetchAdminData(userId!!)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data setiap kali kembali ke halaman ini (biar update setelah edit)
        userId?.let { fetchAdminData(it) }
    }

    private fun fetchAdminData(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Admin"
                    val email = document.getString("email") ?: auth.currentUser?.email ?: "-"
                    val phone = document.getString("phone") ?: "-"
                    val address = document.getString("address") ?: "-"
                    val birthdate = document.getString("birthdate") ?: "-"

                    // Update UI
                    tvName.text = name
                    tvEmail.text = email
                    tvPhone.text = formatPhoneNumber(phone)
                    tvAddress.text = address
                    tvBirthdate.text = birthdate
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal mengambil data profil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showChangePasswordDialog() {
        val email = auth.currentUser?.email
        if (email != null) {
            AlertDialog.Builder(requireContext())
                .setTitle("Ganti Password")
                .setMessage("Kirim link reset password ke email $email?")
                .setPositiveButton("Kirim") { _, _ ->
                    auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Link reset terkirim ke email", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Gagal mengirim link", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari akun admin?")
            .setPositiveButton("Ya") { _, _ ->
                forceLogout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun forceLogout() {
        // 1. Firebase logout
        auth.signOut()

        // 2. Hapus SharedPreferences session
        val prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // 3. Kembali ke LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun formatPhoneNumber(phone: String): String {
        // Hapus karakter non-digit biar bersih
        val digits = phone.filter { it.isDigit() }
        if (digits.isEmpty()) return phone

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