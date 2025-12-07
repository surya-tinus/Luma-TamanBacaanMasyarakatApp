package com.example.luma

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.luma.database.Loan
import com.example.luma.databinding.FragmentBorrowedBinding
import com.example.luma.database.viewmodels.BookViewModel // Pastikan package ini benar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText

class BorrowedFragment : Fragment() {

    private var _binding: FragmentBorrowedBinding? = null
    private val binding get() = _binding!!

    // Simpan filter saat ini ('active' atau 'returned')
    private var currentFilter = "active"

    // Simpan data mentah dari Firebase (sebelum difilter)
    private var allLoans: List<Loan> = emptyList()

    private lateinit var adapter: LoanAdapter
    private lateinit var bookViewModel: BookViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBorrowedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi ViewModel
        bookViewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]

        // 2. Setup RecyclerView & Adapter
        binding.rvBorrowedBooks.layoutManager = LinearLayoutManager(requireContext())

        // Logic Adapter: Cek status untuk menentukan aksi (Return atau Rate)
        adapter = LoanAdapter(emptyList()) { loan ->
            if (loan.status == "active") {
                // Kalo status Active -> Tampilkan Dialog Return
                showReturnConfirmation(loan)
            } else {
                // Kalo status Returned -> Tampilkan Dialog Rating
                showReviewDialog(loan)
            }
        }
        binding.rvBorrowedBooks.adapter = adapter

        // 3. Setup ChipGroup (Tab Switcher)
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.chipActive -> {
                        currentFilter = "active"
                        applyFilter() // Refresh list sesuai filter baru
                    }
                    R.id.chipHistory -> {
                        currentFilter = "returned"
                        applyFilter() // Refresh list sesuai filter baru
                    }
                }
            }
        }

        // 4. Ambil Username & Request Data
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null)

        if (username != null) {
            bookViewModel.fetchUserLoans(username)
        } else {
            Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
        }

        // 5. Observasi Data dari Firebase
        bookViewModel.userLoans.observe(viewLifecycleOwner) { loans ->
            // Simpan data mentah ke variabel global
            allLoans = loans
            // Terapkan filter (Active/History) lalu tampilkan
            applyFilter()
        }
    }

    // --- FUNGSI FILTER DATA ---
    private fun applyFilter() {
        // Ambil data yang statusnya sesuai filter
        val filteredList = allLoans.filter { it.status == currentFilter }
        val sortedList = filteredList.sortedByDescending { it.borrowDate }

        adapter.updateData(sortedList)

        // --- LOGIKA EMPTY STATE ---
        if (sortedList.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            if (currentFilter == "active") {
                binding.tvEmptyState.text = "Kamu belum meminjam buku apapun."
            } else {
                binding.tvEmptyState.text = "Belum ada buku yang dikembalikan."
            }
        } else {
            binding.tvEmptyState.visibility = View.GONE
        }
    }

    // --- FUNGSI DIALOG RETURN (PENGEMBALIAN) ---
    private fun showReturnConfirmation(loan: Loan) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Kembalikan Buku")
            .setMessage("Apakah kamu yakin ingin mengembalikan buku '${loan.bookTitle}'?")
            .setPositiveButton("Ya, Kembalikan") { _, _ ->
                bookViewModel.returnBook(loan)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // --- FUNGSI DIALOG REVIEW (RATING) ---
    private fun showReviewDialog(loan: Loan) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_review, null)

        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val etReview = view.findViewById<TextInputEditText>(R.id.etReview)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmitReview)

        // Kalau user sudah pernah kasih rating sebelumnya, tampilkan lagi
        if (loan.userRating > 0) {
            ratingBar.rating = loan.userRating
            etReview.setText(loan.userReview)
            btnSubmit.text = "Update Ulasan"
        }

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating
            val review = etReview.text.toString()

            if (rating > 0) {
                // Ambil username dari SharedPreferences
                val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val userName = sharedPref.getString("username", "Anonymous") ?: "Anonymous"

                // Panggil fungsi submit review dengan username
                bookViewModel.submitReview(loan, rating, review, userName)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Kasih bintang dulu dong!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}