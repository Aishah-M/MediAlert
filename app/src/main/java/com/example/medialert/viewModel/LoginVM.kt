package com.example.medialert.viewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginVM : ViewModel() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _successMessage = mutableStateOf<String?>(null)
    val successMessage: State<String?> = _successMessage

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
        _successMessage.value = null

        // Find the user's real email from Firestore using their IC
        db.collection("patients")
            .whereEqualTo("icNumber", icNumber)
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots.isEmpty) {
                    _isLoading.value = false
                    _errorMessage.value = "No. IC tidak dijumpai"
                } else {
                    val email = snapshots.documents[0].getString("email")
                    if (email != null) {
                        // Login to Firebase Auth with the retrieved real email
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                _isLoading.value = false
                                if (task.isSuccessful) {
                                    onSuccess()
                                } else {
                                    _errorMessage.value = "No. IC atau Kata Laluan salah"
                                }
                            }
                    } else {
                        _isLoading.value = false
                        _errorMessage.value = "Alamat e-mel tidak dijumpai untuk akaun ini."
                    }
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _errorMessage.value = "Ralat pelayan: ${e.localizedMessage}"
            }
    }

    /**
     * Resets password using the provided email address
     */
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Sila masukkan alamat e-mel anda"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Format e-mel tidak sah"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _successMessage.value = "E-mel set semula kata laluan telah dihantar ke e-mel anda."
                } else {
                    _errorMessage.value = task.exception?.localizedMessage ?: "Gagal menghantar e-mel set semula. Sila semak semula e-mel anda."
                }
            }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
