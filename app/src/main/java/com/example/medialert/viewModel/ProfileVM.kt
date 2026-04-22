package com.example.medialert.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.medialert.data.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileVM : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userProfile = mutableStateOf<UserProfile?>(null)
    val userProfile: State<UserProfile?> = _userProfile

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        db.collection("patients")
            .document(userId)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    Log.e("ProfileVM", "Firestore Error: ${e.message}", e)
                    _error.value = e.localizedMessage
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    _userProfile.value = snapshot.toObject(UserProfile::class.java)
                }
            }
    }

    fun updateUserProfile(updatedProfile: UserProfile, onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        // Ensure fullName and icNumber are not changed if you want to be safe
        // though the UI already handles readOnly for them.
        db.collection("patients")
            .document(userId)
            .set(updatedProfile)
            .addOnSuccessListener {
                _isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.localizedMessage
                Log.e("ProfileVM", "Update Error: ${e.message}", e)
            }
    }
}
