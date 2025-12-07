package com.example.luma

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.luma.database.User
import com.example.luma.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.google.android.material.textfield.TextInputEditText

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvBirthdate: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnChangePass: Button // Tambahkan ini
    private lateinit var btnLogout: Button

    private lateinit var userViewModel: UserViewModel
    private lateinit var sharedPref: SharedPreferences

    private var currentUser: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        tvName = view.findViewById(R.id.tvUserName)
        tvEmail = view.findViewById(R.id.tvUserEmail)
        tvPhone = view.findViewById(R.id.tvUserPhone)
        tvAddress = view.findViewById(R.id.tvUserAddress)
        tvBirthdate = view.findViewById(R.id.tvUserBirthdate)
        btnEdit = view.findViewById(R.id.btnEditProfile)
        btnChangePass = view.findViewById(R.id.btnChangePass) // Pastikan ID ini ada di XML nanti
        btnLogout = view.findViewById(R.id.btnLogout)

        val uid = sharedPref.getString("userId", null)
        if (uid != null) {
            userViewModel.fetchUserProfile(uid)
        } else {
            logoutUser()
        }

        userViewModel.userResult.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentUser = user
                displayData(user)
            }
        }

        btnEdit.setOnClickListener {
            if (currentUser != null) {
                val bundle = Bundle().apply {
                    putString("uid", currentUser!!.id)
                    putString("name", currentUser!!.username)
                    putString("email", currentUser!!.email)
                    putString("phone", currentUser!!.phone)
                    putString("address", currentUser!!.address)
                    putString("birthdate", currentUser!!.birthdate)
                }
                try {
                    findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment, bundle)
                } catch (e: Exception) {
                    findNavController().navigate(R.id.editProfileFragment, bundle)
                }
            }
        }

        // Tombol Ganti Password
        btnChangePass.setOnClickListener {
            showChangePasswordDialog()
        }

        // Observasi status ganti password
        userViewModel.passwordUpdateStatus.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                userViewModel.resetPasswordStatus()
            }
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    } // <--- KURUNG KURAWAL TUTUP onViewCreated DI SINI

    // FUNGSI DI BAWAH INI HARUS DI LUAR onViewCreated
    private fun showChangePasswordDialog() {
        // 1. Inflate Layout Custom Kita
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_password, null)

        // 2. Hubungkan View (EditText & Button dari XML kita)
        val etOldPass = dialogView.findViewById<TextInputEditText>(R.id.etOldPass)
        val etNewPass = dialogView.findViewById<TextInputEditText>(R.id.etNewPass)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSavePass)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelPass)

        // 3. Bikin Dialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)

        // Buat object dialog-nya (jangan di-show dulu)
        val dialog = builder.create()

        // 4. JURUS RAHASIA: Set Background Transparan
        // Ini biar kotak hijau/putih bawaan android hilang, jadi cuma sisa CardView kita
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 5. Logika Tombol Simpan
        btnSave.setOnClickListener {
            val oldPass = etOldPass.text.toString()
            val newPass = etNewPass.text.toString()

            if (oldPass.isNotEmpty() && newPass.isNotEmpty()) {
                userViewModel.changePassword(oldPass, newPass)
                dialog.dismiss() // Tutup dialog setelah klik simpan
            } else {
                Toast.makeText(context, "Isi semua kolom!", Toast.LENGTH_SHORT).show()
            }
        }

        // 6. Logika Tombol Batal
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // 7. Tampilkan
        dialog.show()
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
        FirebaseAuth.getInstance().signOut()
        val editor = sharedPref.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
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