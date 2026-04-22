package com.example.medialert.data

import com.example.medialert.R
import com.google.firebase.Timestamp

object SampleData {
    // Core Medications Library (The "Source of Truth")
    val availableMedications = listOf(
        Medication(
            name = "Amoxicillin 250mg",
            imageRes = R.drawable.mica
        ),
        Medication(
            name = "Paracetamol 500mg",
            imageRes = R.drawable.mica
        ),
        Medication(
            name = "Metformin",
            imageRes = R.drawable.mica
        )
    )

    // Sample Reminders for ReminderScreen & ReminderEditScreen
    val medicationReminders = listOf(
        Reminder(
            medication = availableMedications[0], // Amoxicillin
            dosage = "1",
            unit = "capsule(s)",
            totalStock = 30,
            remainingStock = 12,
            times = listOf("08:00 AM", "08:00 PM")
        ),
        Reminder(
            medication = availableMedications[1], // Paracetamol
            dosage = "2",
            unit = "pill(s)",
            totalStock = 20,
            remainingStock = 3, // This will show as "Low Stock" in UI
            times = listOf("10:00 AM", "02:00 PM", "06:00 PM", "10:00 PM")
        )
    )
}
