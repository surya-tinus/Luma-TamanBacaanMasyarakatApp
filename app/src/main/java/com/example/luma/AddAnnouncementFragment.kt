package com.example.luma

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.luma.database.Announcement
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AddAnnouncementFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private var selectedDate: Date = Date()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_add_announcement, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val etTitle = view.findViewById<EditText>(R.id.etAddTitle)
        val etContent = view.findViewById<EditText>(R.id.etAddContent)
        val etDate = view.findViewById<EditText>(R.id.etAddDate)
        val btnSave = view.findViewById<Button>(R.id.btnSaveAnnouncement)

        // set default date
        etDate.setText(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedDate))

        // Date picker
        etDate.setOnClickListener {
            val cal = Calendar.getInstance()

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    cal.set(year, month, day)
                    selectedDate = cal.time
                    etDate.setText(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedDate))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val announcement = Announcement(
                title = title,
                content = content,
                date = selectedDate
            )

            db.collection("announcements")
                .add(announcement)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Announcement added!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
        }

        view.findViewById<Button>(R.id.btnCancelAnnouncement).setOnClickListener {
            findNavController().popBackStack() // Kembali ke halaman sebelumnya
        }
    }
}