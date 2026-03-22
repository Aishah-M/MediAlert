package com.example.medialert.data

import com.example.medialert.R

object SampleData {
    // Core Medications Library (The "Source of Truth")
    val availableMedications = listOf(
        Medication(
            name = "Amoxicillin 250mg",
            imageRes = R.drawable.mica2
        ),
        Medication(
            name = "Paracetamol 500mg",
            imageRes = R.drawable.mica2
        ),
        Medication(
            name = "Metformin",
            imageRes = R.drawable.mica2
        )
    )

    // medication
    val medications = listOf(
        Medication(
            name = "Amoxicillin 250mg",
            dosage = "1 Biji",
            frequency = "3 kali sehari",
            duration = "7 hari",
            purpose = "Antibiotik untuk jangkitan",
            instructions = "Mesti dihabiskan. Selepas makan.",
            prescriptionDate = "15/03/2026",
            doctorName = "Dr. Ahmad Syah"
        ),
        Medication(
            name = "Paracetamol 500mg",
            dosage = "2 Biji",
            frequency = "4 kali sehari (Jika perlu)",
            duration = "3 hari",
            purpose = "Tahan sakit / Demam",
            instructions = "Maksimum 8 biji sehari.",
            prescriptionDate = "15/03/2026",
            doctorName = "Dr. Ahmad Syah"
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

    // Sample Appointments for AppointmentScreen
    val appointments = listOf(
        Appointment(
            date = "16/03/2026",
            day = "ISNIN",
            time = "09:00 AM",
            department = "Klinik Pakar Pembedahan Am (SOPD)",
            hospital = "Hospital Tanjung Karang",
            reason = "Pemeriksaan berkala",
            status = "Akan datang"
        ),
        Appointment(
            date = "20/04/2026",
            day = "KHAMIS",
            time = "11:30 AM",
            department = "Jabatan Ortopedik",
            hospital = "Hospital Sungai Buloh",
            reason = "Rawatan susulan",
            status = "Selesai"
        )
    )

    val userProfile = UserProfile(
        fullName = "Siti Aminah Binti Sidek",
        email = "siti.aminah@email.com",
        icNumber = "650330-10-5432",
        birthDate = "30 Mac 1965",
        age = "61 Tahun",
        gender = "Perempuan",
        bloodType = "B+",
        phoneNumber = "012-3456789",
        chronicDiseases = "Darah Tinggi, Diabetis",
        allergies = "Makanan Laut",
        emergencyContactName = "Sarah Wayne",
        emergencyContactRelation = "Anak Perempuan",
        emergencyContactPhone = "017-3183544"
    )
}