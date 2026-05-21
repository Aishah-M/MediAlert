package com.example.medialert.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Medication(
    @get:Exclude @set:Exclude
    var id: String = "",

    val name: String = "",
    val unit: String = "",

    @get:PropertyName("imageURL")
    @set:PropertyName("imageURL")
    var imageURL: String = "",

    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",

    val times: List<String> = emptyList(),
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val untilFinish: Boolean = false,

    val purpose: String = "",

    @get:PropertyName("instructions")
    @set:PropertyName("instructions")
    var instructions: String = "",

    val prescriptionDate: Timestamp? = null,
    val doctorName: String = ""
) {
    // UI Compatibility properties
    @get:Exclude
    val imageUrl: String get() = imageURL
    
    @get:Exclude
    val instruction: String get() = instructions
}
