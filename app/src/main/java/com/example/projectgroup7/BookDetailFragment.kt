package com.example.projectgroup7

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class BookDetailFragment : Fragment() {
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_detail, container, false)

        val bookTitle = arguments?.getString("title") ?: ""
        val bookCategory = arguments?.getString("category") ?: ""

        val tvTitle = view.findViewById<TextView>(R.id.tv_book_title)
        val tvCategory = view.findViewById<TextView>(R.id.tv_book_category)
        val tvDescription = view.findViewById<TextView>(R.id.tv_book_description)
        val ivCover = view.findViewById<ImageView>(R.id.iv_book_cover)

        tvTitle.text = bookTitle
        tvCategory.text = bookCategory

        // Panggil API Google Books berdasarkan judul
        CoroutineScope(Dispatchers.IO).launch {
            val url = "https://www.googleapis.com/books/v1/volumes?q=intitle:${bookTitle}"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()

            body?.let {
                val json = JSONObject(it)
                val items = json.optJSONArray("items")
                if (items != null && items.length() > 0) {
                    val volumeInfo = items.getJSONObject(0).getJSONObject("volumeInfo")
                    val description = volumeInfo.optString("description", "No description available")
                    val imageLinks = volumeInfo.optJSONObject("imageLinks")
                    val thumbnail = imageLinks?.optString("thumbnail", "")?.replace("http://", "https://")

                    requireActivity().runOnUiThread {
                        tvDescription.text = description
                        if (!thumbnail.isNullOrEmpty()) {
                            Glide.with(this@BookDetailFragment)
                                .load(thumbnail)
                                .into(ivCover)
                        }
                    }
                }
            }
        }

        // Tombol back
        val backButton = view.findViewById<ImageView>(R.id.btn_back)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }
}
