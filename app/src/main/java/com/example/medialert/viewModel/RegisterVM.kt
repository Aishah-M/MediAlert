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
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        if (icNumber.isBlank() || fullName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _errorMessage.value = "Sila isi semua maklumat"
            return
        }

        if (icNumber.length != 12) {
            _errorMessage.value = "No. IC mestilah 12 digit"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Format e-mel tidak sah"
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

        // 1. Look up the patients collection in Firestore to see if a document with that IC already exists.
        db.collection("patients")
            .whereEqualTo("icNumber", icNumber)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // 2. If it does not exist, show an error: 'IC not found. Please register at the clinic first.'
                    _isLoading.value = false
                    _errorMessage.value = "No. IC tidak dijumpai. Sila daftar di klinik terlebih dahulu."
                } else {
                    val patientDoc = querySnapshot.documents[0]
                    val docId = patientDoc.id

                    // 3. If it does exist, proceed to create a new user in Firebase Authentication
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                val firebaseUid = auth.currentUser?.uid
                                if (firebaseUid != null) {
                                    // 4. After successful authentication creation, update the existing Firestore document
                                    val updates = hashMapOf<String, Any>(
                                        "userId" to firebaseUid,
                                        "email" to email,
                                        "fullName" to fullName
                                    )
                                    
                                    db.collection("patients").document(docId)
                                        .update(updates)
                                        .addOnSuccessListener {
                                            _isLoading.value = false
                                            onSuccess()
                                        }
                                        .addOnFailureListener { e ->
                                            _isLoading.value = false
                                            _errorMessage.value = "Gagal mengemaskini profil: ${e.localizedMessage}"
                                        }
                                }
                            } else {
                                _isLoading.value = false
                                // Firebase will automatically handle "Email already in use" errors here
                                _errorMessage.value = authTask.exception?.localizedMessage ?: "Pendaftaran gagal"
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _errorMessage.value = "Ralat carian IC: ${e.localizedMessage}"
            }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
