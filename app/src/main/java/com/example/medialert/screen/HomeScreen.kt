package com.example.medialert.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medialert.R
import com.example.medialert.data.Reminder
import com.example.medialert.data.UserProfile
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(
    user: UserProfile = com.example.medialert.data.SampleData.userProfile,
    nextAppointment: com.example.medialert.data.Appointment? = com.example.medialert.data.SampleData.appointments.firstOrNull(),
    onProfileClick: () -> Unit,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Local state for medication list to demonstrate stock deduction
    var reminders by remember { mutableStateOf(com.example.medialert.data.SampleData.medicationReminders) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.3f
                )
            )
    ) {
        // 1. Top Custom App Bar
        HomeHeader(user, onProfileClick)

        Column(modifier = Modifier.padding(13.dp)) {
            // 2. Appointment Countdown Card
            AppointmentCountdownCard(nextAppointment)

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Medication Tracking Section
            Text(
                text = "Jadual Ubat Hari Ini",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(reminders) { reminder ->
                    MedicationTaskCard(
                        reminder = reminder,
                        onTakenClick = {
                            // Inventory Logic: Deduct stock locally
                            reminders = reminders.map {
                                if (it.id == reminder.id) it.takeMedication() else it
                            }
                        }
                    )
                }
            }

            // 4. Emergency Call Button (Floating style at bottom)
            LargeEmergencyButton(onClick = onContactClick)
        }
    }
}

@Composable
fun HomeHeader(user: UserProfile, onProfileClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // onPrimary ensures text is readable against the primary blue background
                Text(
                    text = "Hai,",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Text(
                    text = user.fullName,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "${user.age} • ${user.gender}",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }

            // Profile Icon
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape)
                    // Background of the circle adapts to theme
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.person_24dp_000000),
                    contentDescription = "Profile",
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun AppointmentCountdownCard(appointment: com.example.medialert.data.Appointment?) {
    // If no appointment exists, we can show a placeholder or hide the card
    if (appointment == null) return

    // Calculate days remaining
    val daysRemaining = remember(appointment.date) {
        try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val appointmentDate = LocalDate.parse(appointment.date, formatter)
            val today = LocalDate.now()

            // Calculate difference
            val diff = ChronoUnit.DAYS.between(today, appointmentDate)

            // If the date has passed, just show 0
            if (diff > 0) diff else 0L
        } catch (e: Exception) {
            0L
        }
    }
    

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Temujanji Seterusnya",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                // Dynamic Data from Firebase/SampleData
                Text(appointment.hospital, fontWeight = FontWeight.Bold)
                Text(appointment.department, fontSize = 12.sp)
                Text("${appointment.date} • ${appointment.time}", fontSize = 12.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (daysRemaining > 0) daysRemaining.toString() else "0",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (daysRemaining == 1L) "HARI LAGI" else "HARI LAGI",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MedicationTaskCard(reminder: Reminder, onTakenClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.medication?.name ?: "Ubat",
                    fontWeight = FontWeight.Bold,
                    color = if (reminder.isTaken) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${reminder.dosage} ${reminder.unit} • ${reminder.times.firstOrNull() ?: ""}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Baki Stok: ${reminder.remainingStock}",
                    color = if (reminder.remainingStock < 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Dynamic Button based on isTaken state
            Button(
                onClick = onTakenClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    // If taken: Use a subtle grey. If not: Use the secondary color
                    containerColor = if (reminder.isTaken)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (reminder.isTaken)
                        MaterialTheme.colorScheme.outline
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    // If taken: Filled icon. If not: Outlined icon
                    imageVector = if (reminder.isTaken)
                        Icons.Filled.CheckCircle
                    else
                        Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (reminder.isTaken) "Diambil" else "Belum ambil",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LargeEmergencyButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(50.dp), contentPadding = PaddingValues(13.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(painter = painterResource(id =R.drawable.baseline_call_24),
            contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text("HUBUNGI FASILITI KESIHATAN", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    com.example.medialert.theme.MediAlertTheme {
        // A Surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(
                user = com.example.medialert.data.SampleData.userProfile,
                onProfileClick = { /* No-op for preview */ },
                onContactClick = { /* No-op for preview */ }
            )
        }
    }
}

@Preview(showBackground = true,  uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun HomeScreenDarkPreview() {
    com.example.medialert.theme.MediAlertTheme {
        // A Surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(
                user = com.example.medialert.data.SampleData.userProfile,
                onProfileClick = { /* No-op for preview */ },
                onContactClick = { /* No-op for preview */ }
            )
        }
    }
}

@Preview(showBackground = true, name = "Medication Item Preview")
@Composable
fun MedicationItemPreview() {
    com.example.medialert.theme.MediAlertTheme {
        PaddingValues(16.dp).let {
            MedicationTaskCard(
                reminder = com.example.medialert.data.SampleData.medicationReminders[0],
                onTakenClick = {}
            )
        }
    }
}
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun MedicationItemDarkPreview() {
    com.example.medialert.theme.MediAlertTheme {
        MedicationTaskCard(
            reminder = com.example.medialert.data.SampleData.medicationReminders[0],
            onTakenClick = {}
        )
    }
}