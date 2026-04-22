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
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Appointment::class.java)?.copy(id = doc.id)
                    }.sortedWith { a1, a2 ->
                        val t1 = a1.timestamp
                        val t2 = a2.timestamp
                        
                        if (a1.status == a2.status) {
                            if (a1.status == "Akan datang") {
                                // Upcoming: Soonest at top (Ascending)
                                when {
                                    t1 == null && t2 == null -> 0
                                    t1 == null -> 1
                                    t2 == null -> -1
                                    else -> t1.compareTo(t2)
                                }
                            } else {
                                // Selesai: Recent at top, oldest at bottom (Descending)
                                when {
                                    t1 == null && t2 == null -> 0
                                    t1 == null -> 1
                                    t2 == null -> -1
                                    else -> t2.compareTo(t1)
                                }
                            }
                        } else {
                            // "Akan datang" comes before "Selesai"
                            if (a1.status == "Akan datang") -1 else 1
                        }
                    }
                    _appointments.value = list
                }
            }
    }
}
