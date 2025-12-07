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

class EditAnnouncementFragment : Fragment() {

    private lateinit var announcement: Announcement
    private val db = FirebaseFirestore.getInstance()
    private var selectedDate: Date = Date()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_edit_announcement, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        announcement = requireArguments().getParcelable("announcement")!!
        selectedDate = announcement.date

        val etTitle = view.findViewById<EditText>(R.id.etEditTitle)
        val etContent = view.findViewById<EditText>(R.id.etEditContent)
        val etDate = view.findViewById<EditText>(R.id.etEditDate)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdateAnnouncement)

        etTitle.setText(announcement.title)
        etContent.setText(announcement.content)
        etDate.setText(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedDate))

        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.time = selectedDate

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

        btnUpdate.setOnClickListener {

            val newTitle = etTitle.text.toString()
            val newContent = etContent.text.toString()

            if (newTitle.isEmpty() || newContent.isEmpty()) {
                Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("announcements")
                .document(announcement.id)
                .update(
                    mapOf(
                        "title" to newTitle,
                        "content" to newContent,
                        "date" to selectedDate
                    )
                )
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Updated!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
        }
    }
}