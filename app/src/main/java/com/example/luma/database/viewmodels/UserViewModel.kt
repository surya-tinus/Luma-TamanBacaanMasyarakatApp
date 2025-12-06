package com.example.luma.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.luma.database.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // LiveData untuk memantau hasil Login/Register
    private val _userResult = MutableLiveData<User?>()
    val userResult: LiveData<User?> = _userResult

    private val _errorMsg = MutableLiveData<String?>()
    val errorMsg: LiveData<String?> = _errorMsg

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // --- FUNGSI REGISTER ---
    // Menerima data lengkap + password terpisah
    fun register(user: User, password: String) {
        _isLoading.value = true

        // 1. Buat Akun di Firebase Auth
        auth.createUserWithEmailAndPassword(user.email, password)
            .addOnSuccessListener { authResult ->
                // Akun jadi! Ambil UID-nya
                val uid = authResult.user?.uid
                if (uid != null) {
                    // 2. Simpan Biodata Lengkap ke Firestore (koleksi 'users')
                    val newUser = user.copy(id = uid)

                    db.collection("users").document(uid).set(newUser)
                        .addOnSuccessListener {
                            _isLoading.value = false
                            _userResult.value = newUser // Sukses
                        }
                        .addOnFailureListener { e ->
                            _isLoading.value = false
                            _errorMsg.value = "Gagal simpan biodata: ${e.message}"
                        }
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _errorMsg.value = "Register Gagal: ${e.message}"
            }
    }

    // --- FUNGSI LOGIN ---
    fun login(email: String, pass: String) {
        _isLoading.value = true

        // 1. Cek Email & Password ke Firebase Auth
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    // 2. Login Auth Sukses -> Ambil Biodata dari Firestore
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { document ->
                            val loggedInUser = document.toObject(User::class.java)
                            _isLoading.value = false
                            _userResult.value = loggedInUser
                        }
                        .addOnFailureListener {
                            _isLoading.value = false
                            _errorMsg.value = "Gagal ambil data user"
                        }
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _errorMsg.value = "Login Gagal: ${e.message}"
            }
    }

    // Reset status setelah navigasi
    fun doneNavigating() {
        _userResult.value = null
        _errorMsg.value = null
    }

    fun fetchUserProfile(uid: String) {
        _isLoading.value = true

        // Dengarkan perubahan data user secara realtime
        db.collection("users").document(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _errorMsg.value = "Gagal ambil profil: ${e.message}"
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    _userResult.value = user
                }
                _isLoading.value = false
            }
    }}