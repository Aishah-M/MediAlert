package com.example.medialert.data

import androidx.annotation.DrawableRes
import com.google.firebase.Timestamp

data class Medication(
    val id: String = "", // The ID of this prescription record

    // Link to master data (optional but good for tracking)
    val medicineId: String = "", 

    // Snapshot of data (Safe & Fast)
    val name: String = "", 
    val unit: String = "", 

    @DrawableRes val imageRes: Int? = null,
    val imageUrl: String = "",

    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",
    
    // New scheduling fields
    val times: List<String> = emptyList(), // e.g., ["09:00 AM", "01:00 PM"]
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val untilFinish: Boolean = false,

    val purpose: String = "",
    val instructions: String = "",
    val prescriptionDate: String = "",
    val doctorName: String = ""
)
