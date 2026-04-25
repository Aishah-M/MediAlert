package com.example.medialert.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.medialert.data.Reminder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReminderVM : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _reminders = mutableStateOf<List<Reminder>>(emptyList())
    val reminders: State<List<Reminder>> = _reminders

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        fetchReminders()
    }

    fun fetchReminders() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        db.collection("patients")
            .document(userId)
            .collection("reminders")
            .orderBy("medicationName", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    Log.e("ReminderVM", "Firestore Error: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Reminder::class.java)?.copy(id = doc.id)
                    }
                    _reminders.value = list
                }
            }
    }

    fun saveReminder(reminder: Reminder, onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        db.collection("patients")
            .document(userId)
            .collection("reminders")
            .document(reminder.id)
            .set(reminder)
            .addOnSuccessListener {
                _isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                Log.e("ReminderVM", "Save Error: ${e.message}")
            }
    }

    fun deleteReminder(reminderId: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("patients")
            .document(userId)
            .collection("reminders")
            .document(reminderId)
            .delete()
    }
}
