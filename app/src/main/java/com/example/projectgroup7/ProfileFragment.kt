package com.example.projectgroup7

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnEdit = view.findViewById<Button>(R.id.btnEditProfile)
        btnEdit.setOnClickListener {
            Toast.makeText(requireContext(), "Edit Profile feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}
