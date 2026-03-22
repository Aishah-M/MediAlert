package com.example.medialert.data

import androidx.annotation.DrawableRes

data class Medication(
    // 1. Identification
    val id: String = java.util.UUID.randomUUID().toString(),

    // 2. Core Info
    val name: String = "",

    // 3. Image Support (Supports both local drawables and Firebase URLs)
    @DrawableRes val imageRes: Int? = null,
    val imageUrl: String = "",

    // 4. Prescription Details (From your MedicationScreen)
    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",
    val purpose: String = "",
    val instructions: String = "",
    val prescriptionDate: String = "",
    val doctorName: String = ""
)