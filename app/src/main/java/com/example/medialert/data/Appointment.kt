package com.example.medialert.data

import com.google.firebase.Timestamp

data class Appointment(
    val id: String = "",
    val timestamp: Timestamp? = null,
    val department: String = "",
    val reason: String = "",
    val status: String = "" // "Akan datang" or "Selesai"
)
