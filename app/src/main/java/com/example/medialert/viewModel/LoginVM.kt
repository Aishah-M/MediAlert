package com.example.medialert.viewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginVM : ViewModel() {
    // Using 'by lazy' prevents Firebase from initializing during Compose Previews
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun loginPatient(icNumber: String, password: String, onSuccess: () -> Unit) {
        if (icNumber.isBlank() || password.isBlank()) {
            _errorMessage.value = "Sila isi semua maklumat"
            return
        }

        if (icNumber.length != 12) {
            _errorMessage.value = "No. IC mestilah 12 digit (Tanpa '-')"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        // The Masking Trick: Turn IC into the hidden email format
        val fakeEmail = "$icNumber@medialert.com"

        auth.signInWithEmailAndPassword(fakeEmail, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    _errorMessage.value = "No. IC atau Kata Laluan salah"
                }
            }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
