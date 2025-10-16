package com.example.projectgroup7

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var userName = "User123456"
    private var userEmail = "user123456@example.com"
    private var userPhone = "081234567890"
    private var userAddress = "Scientia Boulevard Gading, Curug Sangereng"
    private var userBirthdate = "29 May 2000"

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvBirthdate: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvName = view.findViewById(R.id.tvUserName)
        tvEmail = view.findViewById(R.id.tvUserEmail)
        tvPhone = view.findViewById(R.id.tvUserPhone)
        tvAddress = view.findViewById(R.id.tvUserAddress)
        tvBirthdate = view.findViewById(R.id.tvUserBirthdate)
        val btnEdit = view.findViewById<Button>(R.id.btnEditProfile)

        showProfileData()

        // Navigasi ke EditProfileFragment
        btnEdit.setOnClickListener {
            val bundle = Bundle().apply {
                putString("name", userName)
                putString("email", userEmail)
                putString("phone", userPhone)
                putString("birthdate", userBirthdate)
            }
            findNavController().navigate(R.id.editProfileFragment, bundle)
        }

        // Ambil data hasil edit
        parentFragmentManager.setFragmentResultListener("profile_update", viewLifecycleOwner) { _, bundle ->
            userName = bundle.getString("name") ?: userName
            userEmail = bundle.getString("email") ?: userEmail
            userPhone = bundle.getString("phone") ?: userPhone
            userBirthdate = bundle.getString("birthdate") ?: userBirthdate
            showProfileData()
        }
    }

    private fun showProfileData() {
        tvName.text = userName
        tvEmail.text = userEmail
        tvPhone.text = formatPhoneNumber(userPhone)
        tvAddress.text = userAddress
        tvBirthdate.text = userBirthdate
    }

    // Fungsi tambahan untuk format nomor dengan strip
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