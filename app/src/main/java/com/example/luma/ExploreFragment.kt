package com.example.luma

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luma.database.Book // 1. Pastikan pakai Book dari Database
import com.example.luma.database.viewmodels.BookViewModel // 2. Pakai ViewModel

class ExploreFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookAdapter
    private lateinit var bookViewModel: BookViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewExplore)
        // Grid 2 kolom biar mirip explore IG
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Inisialisasi adapter dengan list kosong dulu
        adapter = BookAdapter(emptyList()) { selectedBook ->
            openDetail(selectedBook)
        }
        recyclerView.adapter = adapter

        // 2. Inisialisasi ViewModel
        bookViewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]

        // 3. Ambil Data dari Database & Acak (Randomize)
        bookViewModel.allBooks.observe(viewLifecycleOwner) { books ->
            if (books.isNotEmpty()) {
                // Konsep Explore: Tampilkan buku secara acak
                // Kita pakai .shuffled() untuk mengacak urutan list
                val randomBooks = books.shuffled()

                adapter.updateData(randomBooks)
            }
        }
    }

    private fun openDetail(selectedBook: Book) {
        // Nanti diaktifkan saat DetailFragment sudah siap menerima Parcelable
        Toast.makeText(requireContext(), "Explore: ${selectedBook.title}", Toast.LENGTH_SHORT).show()

        /* // KODE NAVIGASI (Aktifkan nanti):
        // Kirim object Book utuh karena sudah @Parcelize
        val bundle = Bundle().apply {
            putParcelable("DATA_BUKU", selectedBook)
        }
        // Pastikan ID action di nav_graph sudah benar:
        // requireView().findNavController()
        //    .navigate(R.id.action_exploreFragment_to_bookDetailFragment, bundle)
        */
    }
}