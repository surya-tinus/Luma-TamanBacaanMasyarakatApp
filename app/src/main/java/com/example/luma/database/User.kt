package com.example.luma.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "password")
    val password: String,

    @ColumnInfo(name = "phone") // Tambahan sesuai desain kamu
    val phone: String,

    @ColumnInfo(name = "address") // Tambahan sesuai desain kamu
    val address: String,

    @ColumnInfo(name = "birthdate") // Tambahan sesuai desain kamu
    val birthdate: String,

    @ColumnInfo(name = "role")
    val role: String = "member"
)