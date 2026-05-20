package com.example.medialert.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val medicationName: String = "", 
    val dosage: String = "", // Assuming this is a number as string like "1"
    val unit: String = "",
    val totalStock: Int = 0,
    val remainingStock: Int = 0,
    val times: List<String> = emptyList(),
    val isTaken: Boolean = false,
    val takenLog: List<String> = emptyList(), // Stores strings like "yyyy-MM-dd_HH:mm AM"
    val startDate: Long? = null, // Store as milliseconds
    val endDate: Long? = null,   // Store as milliseconds
    val untilFinish: Boolean = false
) {
    fun toggleTakenAt(dateStr: String, timeStr: String): Reminder {
        val entry = "${dateStr}_$timeStr"
        val alreadyTaken = takenLog.contains(entry)
        val newLog = if (alreadyTaken) takenLog - entry else takenLog + entry
        
        // Stock reduction logic: Reduce by dosage if marking as taken
        val dosageInt = dosage.toIntOrNull() ?: 0
        val stockAdj = if (alreadyTaken) dosageInt else -dosageInt

        return this.copy(
            takenLog = newLog,
            remainingStock = (remainingStock + stockAdj).coerceAtLeast(0)
        )
    }
}
