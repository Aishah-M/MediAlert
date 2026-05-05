package com.example.medialert.data

import com.google.firebase.Timestamp
import java.util.UUID

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    
    // These 2 fields only used for patient-added ubat/vitamin
    val medicationName: String = "", 
    
    // Fields for both types
    val dosage: String = "",
    val unit: String = "",
    val totalStock: Int = 0,
    val remainingStock: Int = 0,
    val times: List<String> = emptyList(),
    val isTaken: Boolean = false,
    
    // Scheduling fields
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val untilFinish: Boolean = false
) {
    fun takeMedication(): Reminder {
        return if (!isTaken && remainingStock > 0) {
            this.copy(remainingStock = remainingStock - 1, isTaken = true)
        } else {
            this.copy(isTaken = !isTaken)
        }
    }
}
