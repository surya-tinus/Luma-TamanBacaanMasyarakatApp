package com.example.projectgroup7

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class AdminProfileFragment : Fragment(R.layout.fragment_admin_profile) {

    private var adminName = "Jane Doe"
    private var adminEmail = "janedoe@example.com"
    private var adminPhone = "081298765432"
    private var adminAddress = "Taman Bacaan Kelurahan Curug Sangereng"
    private var adminBirthdate = "22 January 1999"

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvBirthdate: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnLogout: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvName = view.findViewById(R.id.tvAdminName)
        tvEmail = view.findViewById(R.id.tvAdminEmail)
        tvPhone = view.findViewById(R.id.tvAdminPhone)
        tvAddress = view.findViewById(R.id.tvAdminAddress)
        tvBirthdate = view.findViewById(R.id.tvAdminBirthdate)
        btnEdit = view.findViewById(R.id.btnEditProfile)
        btnLogout = view.findViewById(R.id.btnLogout)

        showProfileData()

        // Tombol Edit
        btnEdit.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            intent.putExtra("name", tvName.text.toString())
            intent.putExtra("email", tvEmail.text.toString())
            intent.putExtra("phone", tvPhone.text.toString())
            intent.putExtra("birthdate", tvBirthdate.text.toString())
            startActivity(intent)
        }

        // Tombol Logout dengan dialog konfirmasi
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        // Ambil hasil edit profil
        parentFragmentManager.setFragmentResultListener("profile_update", viewLifecycleOwner) { _, bundle ->
            adminName = bundle.getString("name") ?: adminName
            adminEmail = bundle.getString("email") ?: adminEmail
            adminPhone = bundle.getString("phone") ?: adminPhone
            adminBirthdate = bundle.getString("birthdate") ?: adminBirthdate
            showProfileData()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari akun admin?")
            .setPositiveButton("Ya") { _, _ ->
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showProfileData() {
        tvName.text = adminName
        tvEmail.text = adminEmail
        tvPhone.text = formatPhoneNumber(adminPhone)
        tvAddress.text = adminAddress
        tvBirthdate.text = adminBirthdate
    }

    private fun formatPhoneNumber(phone: String): String {
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