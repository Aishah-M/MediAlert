package com.example.medialert.data

import java.util.UUID

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val medication: Medication? = null,
    val dosage: String = "",
    val unit: String = "",
    val totalStock: Int = 0,      // Change to Int
    val remainingStock: Int = 0,  // Change to Int
    val times: List<String> = emptyList(),
    val isTaken: Boolean = false
) {
    fun takeMedication(): Reminder {
        // Only deduct stock if it wasn't already taken
        return if (!isTaken && remainingStock > 0) {
            this.copy(remainingStock = remainingStock - 1, isTaken = true)
        } else {
            this.copy(isTaken = !isTaken) // Toggle back and forth if needed
        }
    }
}