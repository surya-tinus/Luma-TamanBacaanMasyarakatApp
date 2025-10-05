package com.example.projectgroup7.model

data class BorrowedBook(
    val bookId: Int,
    val title: String,
    val author: String,
    val borrowDate: String,
    val returnDate: String?,    // null if not yet returned
    val status: String,         // "borrowed" or "returned"
    val remainingTime: Long     // milliseconds left (0 if returned)
)
