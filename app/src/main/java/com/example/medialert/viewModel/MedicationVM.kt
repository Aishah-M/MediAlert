package com.example.medialert.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.medialert.data.Medication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MedicationVM : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _medications = mutableStateOf<List<Medication>>(emptyList())
    val medications: State<List<Medication>> = _medications

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        fetchMedicines()
    }

    fun fetchMedicines() {
        val userId = auth.currentUser?.uid ?: return
        Log.d("MedicationVM", "Fetching medications for patient: $userId")

        _isLoading.value = true
        _error.value = null

        // Fetching from patients/{userId}/medications sub-collection
        db.collection("patients")
            .document(userId)
            .collection("medications")
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    Log.e("MedicationVM", "Firestore Error: ${e.message}", e)
                    _error.value = e.localizedMessage
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Medication::class.java)?.copy(id = doc.id)
                    }
                    _medications.value = list
                }
            }
    }
}
