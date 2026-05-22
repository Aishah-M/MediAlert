package com.example.medialert.viewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.medialert.data.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeVM : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userProfile = mutableStateOf<UserProfile?>(null)
    val userProfile: State<UserProfile?> = _userProfile

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        // Find the patient document where the "userId" field matches the Auth UID
        db.collection("patients")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, e ->
                _isLoading.value = false
                if (snapshots != null && !snapshots.isEmpty) {
                    val profile = snapshots.documents[0].toObject(UserProfile::class.java)
                    _userProfile.value = profile
                }
            }
    }
}
