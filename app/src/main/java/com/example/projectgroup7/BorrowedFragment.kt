package com.example.projectgroup7.network

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectgroup7.databinding.FragmentBorrowedBinding
import com.example.projectgroup7.model.BorrowedBook

class BorrowedFragment : Fragment() {

    private var _binding: FragmentBorrowedBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: BorrowedBookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBorrowedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvBorrowedBooks.layoutManager = LinearLayoutManager(requireContext())

        // DUMMY data — replace with API / DB later
        val borrowedBooks = listOf(
            BorrowedBook(
                bookId = 1,
                title = "The Pragmatic Programmer",
                author = "Andrew Hunt",
                borrowDate = "2025-10-01",
                returnDate = null,
                status = "borrowed",
                remainingTime = 3 * 24 * 60 * 60 * 1000L
            ),
            BorrowedBook(
                bookId = 2,
                title = "Clean Code",
                author = "Robert C. Martin",
                borrowDate = "2025-09-29",
                returnDate = null,
                status = "borrowed",
                remainingTime = 1 * 24 * 60 * 60 * 1000L
            ),
            BorrowedBook(
                bookId = 3,
                title = "Design Patterns",
                author = "Erich Gamma",
                borrowDate = "2025-09-20",
                returnDate = "2025-09-27",
                status = "returned",
                remainingTime = 0L
            ),
            BorrowedBook(
                bookId = 4,
                title = "Effective Java",
                author = "Joshua Bloch",
                borrowDate = "2025-09-15",
                returnDate = "2025-09-25",
                status = "returned",
                remainingTime = 0L
            )
        )

        adapter = BorrowedBookAdapter(borrowedBooks)
        binding.rvBorrowedBooks.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
