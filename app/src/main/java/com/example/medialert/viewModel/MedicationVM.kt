package com.example.medialert.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.medialert.data.Medication
import com.example.medialert.service.AlarmScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MedicationVM(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val alarmScheduler = AlarmScheduler(application)

    private val _medications = mutableStateOf<List<Medication>>(emptyList())
    val medications: State<List<Medication>> = _medications

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private var medicationListener: ListenerRegistration? = null

    init {
        fetchMedicines()
    }

    fun fetchMedicines() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true
        _error.value = null

        medicationListener?.remove()

        // 1. First find the patient document that has this userId field
        db.collection("patients")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots.isEmpty) {
                    _isLoading.value = false
                    Log.e("MedicationVM", "No profile found for UID: $userId")
                    return@addOnSuccessListener
                }

                // 2. Access the medications sub-collection from that document
                val patientDocRef = snapshots.documents[0].reference
                
                medicationListener = patientDocRef.collection("medications")
                    .orderBy("name", Query.Direction.ASCENDING)
                    .addSnapshotListener { snapshot, e ->
                        _isLoading.value = false
                        if (e != null) {
                            Log.e("MedicationVM", "Firestore Error: ${e.message}")
                            _error.value = e.localizedMessage
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    doc.toObject(Medication::class.java)?.apply { id = doc.id }
                                } catch (ex: Exception) {
                                    null
                                }
                            }
                            _medications.value = list
                            alarmScheduler.scheduleAllAlarms()
                        }
                    }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.localizedMessage
            }
    }

    override fun onCleared() {
        super.onCleared()
        medicationListener?.remove()
    }
}
