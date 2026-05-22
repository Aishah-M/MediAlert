package com.example.medialert.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.medialert.data.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ProfileVM : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userProfile = mutableStateOf<UserProfile?>(null)
    val userProfile: State<UserProfile?> = _userProfile

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private var profileListener: ListenerRegistration? = null
    private var patientDocId: String? = null // Store the actual Firestore Doc ID (e.g., the IC)

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        profileListener?.remove()

        // Search for the document where the "userId" field matches the Auth UID
        profileListener = db.collection("patients")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, e ->
                _isLoading.value = false
                if (e != null) {
                    Log.e("ProfileVM", "Firestore Error: ${e.message}")
                    _error.value = e.localizedMessage
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val doc = snapshots.documents[0]
                    patientDocId = doc.id // This is the Document ID (could be your IC)
                    _userProfile.value = doc.toObject(UserProfile::class.java)
                } else {
                    _error.value = "Profil tidak dijumpai"
                }
            }
    }

    fun updateUserProfile(updatedProfile: UserProfile, onSuccess: () -> Unit) {
        val docId = patientDocId ?: return
        _isLoading.value = true

        db.collection("patients")
            .document(docId)
            .set(updatedProfile)
            .addOnSuccessListener {
                _isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.localizedMessage
            }
    }

    override fun onCleared() {
        super.onCleared()
        profileListener?.remove()
    }
}
