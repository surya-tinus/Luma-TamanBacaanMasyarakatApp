package com.example.projectgroup7

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
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

        val bookTitle = arguments?.getString("title")
        val bookCategory = arguments?.getString("category")
        val bookImageUrl = arguments?.getString("imageUrl")
        val bookDescription = arguments?.getString("description")

        val titleTextView = view.findViewById<TextView>(R.id.tv_book_title)
        val categoryTextView = view.findViewById<TextView>(R.id.tv_book_category)
        val descriptionTextView = view.findViewById<TextView>(R.id.tv_book_description)
        val imageView = view.findViewById<ImageView>(R.id.iv_book_cover)
        val ratingBar = view.findViewById<RatingBar>(R.id.rating_bar)
        val ratingText = view.findViewById<TextView>(R.id.tv_rating_value)
        val backButton = view.findViewById<ImageView>(R.id.btn_back)

        // Set data awal dari argument
        titleTextView.text = bookTitle
        categoryTextView.text = bookCategory
        descriptionTextView.text = bookDescription

        // Muat gambar dari URL argument
        if (!bookImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(bookImageUrl)
                .into(imageView)
        }

        // Default rating = 0
        ratingBar.rating = 0f
        ratingText.text = "0.0"

        // Panggil Google Books API untuk data tambahan
        if (!bookTitle.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val url = "https://www.googleapis.com/books/v1/volumes?q=intitle:${bookTitle}"
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    val body = response.body?.string()

                    body?.let {
                        val json = JSONObject(it)
                        val items = json.optJSONArray("items")
                        if (items != null && items.length() > 0) {
                            val volumeInfo = items.getJSONObject(0).getJSONObject("volumeInfo")
                            val description =
                                volumeInfo.optString("description", "No description available")
                            val imageLinks = volumeInfo.optJSONObject("imageLinks")
                            val thumbnail = imageLinks?.optString("thumbnail", "")
                                ?.replace("http://", "https://")
                            val averageRating = volumeInfo.optDouble("averageRating", 0.0)

                            requireActivity().runOnUiThread {
                                descriptionTextView.text = description
                                ratingBar.rating = averageRating.toFloat()
                                ratingText.text = String.format("%.1f", averageRating)
                                if (!thumbnail.isNullOrEmpty()) {
                                    Glide.with(this@BookDetailFragment)
                                        .load(thumbnail)
                                        .into(imageView)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        backButton.setOnClickListener {
            Log.d("BookDetailFragment", "Tombol Back Ditekan!")
            findNavController().navigateUp()
        }

        return view
    }
}