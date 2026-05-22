package com.example.medialert.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.medialert.data.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class AppointmentVM : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _appointments = mutableStateOf<List<Appointment>>(emptyList())
    val appointments: State<List<Appointment>> = _appointments

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private var appointmentListener: ListenerRegistration? = null

    init {
        fetchAppointments()
    }

    fun fetchAppointments() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true
        _error.value = null

        appointmentListener?.remove()

        // 1. Find the patient document that has this userId field (Linking Auth to Firestore)
        db.collection("patients")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots.isEmpty) {
                    _isLoading.value = false
                    Log.e("AppointmentVM", "No patient profile found for UID: $userId")
                    return@addOnSuccessListener
                }

                // 2. Use the found document (likely ID is IC) to listen to appointments
                val patientDocRef = snapshots.documents[0].reference
                
                appointmentListener = patientDocRef.collection("appointments")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener { apptSnapshot, e ->
                        _isLoading.value = false
                        if (e != null) {
                            Log.e("AppointmentVM", "Firestore Error: ${e.message}")
                            _error.value = e.localizedMessage
                            return@addSnapshotListener
                        }

                        if (apptSnapshot != null) {
                            val list = apptSnapshot.documents.mapNotNull { doc ->
                                doc.toObject(Appointment::class.java)?.copy(id = doc.id)
                            }.sortedWith { a1, a2 ->
                                val t1 = a1.timestamp
                                val t2 = a2.timestamp
                                
                                if (a1.status == a2.status) {
                                    if (a1.status == "Akan datang") {
                                        when {
                                            t1 == null && t2 == null -> 0
                                            t1 == null -> 1
                                            t2 == null -> -1
                                            else -> t1.compareTo(t2)
                                        }
                                    } else {
                                        when {
                                            t1 == null && t2 == null -> 0
                                            t1 == null -> 1
                                            t2 == null -> -1
                                            else -> t2.compareTo(t1)
                                        }
                                    }
                                } else {
                                    if (a1.status == "Akan datang") -1 else 1
                                }
                            }
                            _appointments.value = list
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
        appointmentListener?.remove()
    }
}
