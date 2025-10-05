package com.example.projectgroup7.model

data class BookResponse(
    val items: List<BookItem>?
)

data class BookItem(
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String?,
    val categories: List<String>?,
    val imageLinks: ImageLinks?,
    val description: String? // <-- Properti ini ditambahkan
)

data class ImageLinks(
    val thumbnail: String?
)
