package com.example.medialert.data

data class Appointment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: String,
    val day: String,
    val time: String,
    val department: String,
    val hospital: String,
    val reason: String,
    val status: String // "Akan datang" or "Selesai"
)