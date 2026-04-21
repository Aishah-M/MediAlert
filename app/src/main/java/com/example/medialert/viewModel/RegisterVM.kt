package com.example.medialert.viewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterVM : ViewModel() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun registerPatient(
        icNumber: String,
        fullName: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        if (icNumber.isBlank() || fullName.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _errorMessage.value = "Sila isi semua maklumat"
            return
        }

        if (icNumber.length != 12) {
            _errorMessage.value = "No. IC mestilah 12 digit"
            return
        }

        if (password != confirmPassword) {
            _errorMessage.value = "Kata laluan tidak sepadan"
            return
        }

        if (password.length < 6) {
            _errorMessage.value = "Kata laluan mestilah sekurang-kurangnya 6 aksara"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        val fakeEmail = "$icNumber@medialert.com"

        auth.createUserWithEmailAndPassword(fakeEmail, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val userMap = hashMapOf(
                            "icNumber" to icNumber,
                            "fullName" to fullName,
                            "userId" to userId
                        )
                        db.collection("patients").document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                _isLoading.value = false
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                _isLoading.value = false
                                _errorMessage.value = e.localizedMessage ?: "Gagal menyimpan data pengguna"
                            }
                    } else {
                        _isLoading.value = false
                        onSuccess()
                    }
                } else {
                    _isLoading.value = false
                    _errorMessage.value = task.exception?.localizedMessage ?: "Pendaftaran gagal"
                }
            }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
