package com.example.projectgroup7

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvBirthdate: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnLogout: Button

    private lateinit var sharedPref: android.content.SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        tvName = view.findViewById(R.id.tvUserName)
        tvEmail = view.findViewById(R.id.tvUserEmail)
        tvPhone = view.findViewById(R.id.tvUserPhone)
        tvAddress = view.findViewById(R.id.tvUserAddress)
        tvBirthdate = view.findViewById(R.id.tvUserBirthdate)
        btnEdit = view.findViewById(R.id.btnEditProfile)
        btnLogout = view.findViewById(R.id.btnLogout)

        showProfileData()

        btnEdit.setOnClickListener {
            val bundle = Bundle().apply {
                putString("name", sharedPref.getString("username", "User"))
                putString("email", sharedPref.getString("username", "User") + "@example.com")
                putString("phone", sharedPref.getString("phone", ""))
                putString("birthdate", sharedPref.getString("birthdate", ""))
            }
            findNavController().navigate(R.id.editProfileFragment, bundle)
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        parentFragmentManager.setFragmentResultListener("profile_update", viewLifecycleOwner) { _, bundle ->
            with(sharedPref.edit()) {
                putString("username", bundle.getString("name"))
                putString("phone", bundle.getString("phone"))
                putString("birthdate", bundle.getString("birthdate"))
                apply()
            }
            showProfileData()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari akun Anda?")
            .setPositiveButton("Ya") { _, _ ->
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showProfileData() {
        val userName = sharedPref.getString("username", "User123456")
        val userEmail = "$userName@example.com"
        val userPhone = sharedPref.getString("phone", "081234567890")
        val userAddress = sharedPref.getString("address", "Scientia Boulevard Gading, Curug Sangereng")
        val userBirthdate = sharedPref.getString("birthdate", "29 May 2000")

        tvName.text = userName
        tvEmail.text = userEmail
        tvPhone.text = formatPhoneNumber(userPhone ?: "")
        tvAddress.text = userAddress
        tvBirthdate.text = userBirthdate
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
