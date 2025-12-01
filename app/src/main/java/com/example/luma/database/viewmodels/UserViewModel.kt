package com.example.luma.database.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.luma.database.AppDatabase
import com.example.luma.database.User
import kotlinx.coroutines.launch

// Kita pakai AndroidViewModel supaya bisa akses 'Application' context untuk buka Database
class UserViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Inisialisasi Database & DAO
    private val repository: com.example.luma.database.UserDao

    init {
        // TAMBAHKAN viewModelScope sebagai parameter kedua
        val userDao = AppDatabase.getDatabase(application, viewModelScope).userDao()
        repository = userDao
    }

    // 2. LiveData untuk memantau hasil Login
    // Activity akan mengamati (observe) variabel ini.
    // Jika isinya berubah (misal: ketemu User-nya), Activity akan bereaksi.
    private val _loginResult = MutableLiveData<User?>()
    val loginResult: LiveData<User?> = _loginResult

    // 3. LiveData untuk memantau status Register (Berhasil/Gagal)
    private val _registerStatus = MutableLiveData<Boolean>()
    val registerStatus: LiveData<Boolean> = _registerStatus

    // --- FUNGSI REGISTER ---
    fun register(user: User) {
        // viewModelScope.launch menjalankan proses di background (asynchronous)
        // supaya UI tidak macet (hang) saat menyimpan data.
        viewModelScope.launch {
            try {
                repository.insertUser(user)
                _registerStatus.value = true // Berhasil
            } catch (e: Exception) {
                _registerStatus.value = false // Gagal (mungkin username kembar/error lain)
            }
        }
    }

    // --- FUNGSI LOGIN ---
    fun login(username: String, pass: String) {
        viewModelScope.launch {
            // Panggil fungsi login yang ada di DAO
            val user = repository.login(username, pass)

            // Update hasil ke LiveData.
            // Jika user ditemukan, isinya Object User. Jika tidak, isinya null.
            _loginResult.value = user
        }
    }

    // Fungsi untuk mereset status login (opsional, berguna saat logout)
    fun logout() {
        _loginResult.value = null
    }
}