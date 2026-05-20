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
        Log.d("MedicationVM", "Fetching medications for patient: $userId")

        _isLoading.value = true
        _error.value = null

        // Remove old listener if exists
        medicationListener?.remove()

        // Fetching from patients/{userId}/medications sub-collection with real-time updates
        medicationListener = db.collection("patients")
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
                    
                    // Trigger alarm scheduling whenever data changes to ensure notifications are set
                    Log.d("MedicationVM", "Medication list updated, rescheduling alarms")
                    alarmScheduler.scheduleAllAlarms()
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        medicationListener?.remove()
    }
}
