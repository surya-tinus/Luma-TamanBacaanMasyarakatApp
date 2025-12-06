package com.example.luma

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.luma.databinding.FragmentBorrowedBinding
import com.example.luma.database.viewmodels.BookViewModel

class BorrowedFragment : Fragment() {

    private var _binding: FragmentBorrowedBinding? = null
    private val binding get() = _binding!!

    // 1. Ganti Adapter ke LoanAdapter (yang kita buat sebelumnya)
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

        // 2. Setup RecyclerView
        binding.rvBorrowedBooks.layoutManager = LinearLayoutManager(requireContext())

        // Setup Adapter dengan Konfirmasi Dialog
        adapter = LoanAdapter(emptyList()) { loan ->
            // Munculkan Dialog Konfirmasi biar user gak kepencet
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Kembalikan Buku")
                .setMessage("Apakah kamu yakin ingin mengembalikan buku '${loan.bookTitle}'?")
                .setPositiveButton("Ya, Kembalikan") { _, _ ->
                    // Panggil fungsi Return di ViewModel
                    bookViewModel.returnBook(loan)
                }
                .setNegativeButton("Batal", null)
                .show()
        }
        binding.rvBorrowedBooks.adapter = adapter

        // 3. Inisialisasi ViewModel
        bookViewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]

        // 4. Ambil Username User yang sedang Login
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null)

        if (username != null) {
            // Minta data peminjaman ke Firebase
            bookViewModel.fetchUserLoans(username)
        } else {
            Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
        }

        // 5. Observasi Data (Realtime update dari Firebase)
        bookViewModel.userLoans.observe(viewLifecycleOwner) { loans ->
            if (loans.isEmpty()) {
                // Opsional: Tampilkan teks kosong jika tidak ada pinjaman
                // binding.tvEmptyState.visibility = View.VISIBLE
            }
            // Update data ke adapter
            adapter.updateData(loans)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}