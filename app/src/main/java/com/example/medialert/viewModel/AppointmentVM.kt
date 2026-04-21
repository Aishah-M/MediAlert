package com.example.medialert.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.medialert.data.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AppointmentVM : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _appointments = mutableStateOf<List<Appointment>>(emptyList())
    val appointments: State<List<Appointment>> = _appointments

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    // Add a state to capture error messages for the UI
    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        fetchAppointments()
    }

    fun fetchAppointments() {
        val userId = auth.currentUser?.uid ?: return
        Log.d("AppointmentVM", "Fetching for UID: $userId")

        _isLoading.value = true
        _error.value = null
        
        db.collection("patients")
            .document(userId)
            .collection("appointments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    Log.e("AppointmentVM", "Firestore Error: ${e.message}", e)
                    _error.value = e.localizedMessage
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("AppointmentVM", "Documents found: ${snapshot.size()}")
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Appointment::class.java)?.copy(id = doc.id)
                    }
                    _appointments.value = list
                }
            }
    }
}
