package com.example.medialert.data

import java.util.Calendar

data class UserProfile(
    val fullName: String = "",
    val email: String = "",
    val icNumber: String = "",
    val userId: String = "",
    val phoneNumber: String = "",
    val birthDate: String = "",
    val age: Int = 0,
    val gender: String = "", // Lelaki or Perempuan
    val bloodType: String = "",
    val chronicDiseases: String = "",
    val allergies: String = "",
    val emergencyContactName: String = "",
    val emergencyContactRelation: String = "",
    val emergencyContactPhone: String = ""
) {
    /**
     * Extracts birth date from IC Number (Format: YYMMDDXXXXXX)
     */
    fun calculateBirthDateFromIC(): String {
        if (icNumber.length < 6) return birthDate
        
        val yy = icNumber.substring(0, 2)
        val mm = icNumber.substring(2, 4)
        val dd = icNumber.substring(4, 6)
        
        val currentYearShort = Calendar.getInstance().get(Calendar.YEAR) % 100
        val century = if (yy.toInt() > currentYearShort) "19" else "20"
        
        return "$dd/$mm/$century$yy"
    }

    /**
     * Calculates age from IC Number
     */
    fun calculateAgeFromIC(): Int {
        if (icNumber.length < 2) return age
        
        val yy = icNumber.substring(0, 2).toInt()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentYearShort = currentYear % 100
        
        val birthYear = if (yy > currentYearShort) 1900 + yy else 2000 + yy
        return currentYear - birthYear
    }

    /**
     * Extracts gender from IC Number (Last digit: odd = Male/Lelaki, even = Female/Perempuan)
     */
    fun calculateGenderFromIC(): String {
        if (icNumber.length < 12) return gender
        val lastDigit = icNumber.last().toString().toInt()
        return if (lastDigit % 2 == 0) "Perempuan" else "Lelaki"
    }
}
