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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medialert.R
import com.example.medialert.data.Reminder
import com.example.medialert.data.UserProfile
import com.example.medialert.data.Appointment
import com.example.medialert.viewModel.AppointmentVM
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

@Composable
fun HomeScreen(
    user: UserProfile = com.example.medialert.data.SampleData.userProfile,
    appointmentViewModel: AppointmentVM = viewModel(),
    onProfileClick: () -> Unit,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appointments by appointmentViewModel.appointments
    // Only pick the first appointment that is "Akan datang"
    val nextAppointment = appointments.find { it.status == "Akan datang" }

    // Local state for medication list to demonstrate stock deduction
    var reminders by remember { mutableStateOf(com.example.medialert.data.SampleData.medicationReminders) }

    HomeContent(
        user = user,
        nextAppointment = nextAppointment,
        reminders = reminders,
        onProfileClick = onProfileClick,
        onContactClick = onContactClick,
        onTakenClick = { reminder ->
            reminders = reminders.map {
                if (it.id == reminder.id) it.takeMedication() else it
            }
        },
        modifier = modifier
    )
}

@Composable
fun HomeContent(
    user: UserProfile,
    nextAppointment: Appointment?,
    reminders: List<Reminder>,
    onProfileClick: () -> Unit,
    onContactClick: () -> Unit,
    onTakenClick: (Reminder) -> Unit,
    modifier: Modifier = Modifier
) {
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
                        onTakenClick = { onTakenClick(reminder) }
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

            Box(
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape)
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
fun AppointmentCountdownCard(appointment: Appointment?) {
    if (appointment == null) return

    val daysRemaining = remember(appointment.timestamp) {
        appointment.timestamp?.toDate()?.let { apptDate ->
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            val diff = apptDate.time - today.time
            val days = diff / (1000 * 60 * 60 * 24)
            if (days > 0) days else 0L
        } ?: 0L
    }

    val dateStr = appointment.timestamp?.toDate()?.let {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
    } ?: "N/A"

    val timeStr = appointment.timestamp?.toDate()?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it).uppercase()
    } ?: "N/A"

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
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(appointment.department, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "$dateStr • $timeStr",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = daysRemaining.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "HARI LAGI",
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

            Button(
                onClick = onTakenClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
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
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(painter = painterResource(id = R.drawable.baseline_call_24),
            contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text("HUBUNGI FASILITI KESIHATAN", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreviewWithData() {
    com.example.medialert.theme.MediAlertTheme {
        HomeContent(
            user = UserProfile(
                fullName = "Siti Aminah Binti Sidek",
                age = "61 Tahun",
                gender = "Perempuan",
                icNumber = "650330105432"
            ),
            nextAppointment = Appointment(
                timestamp = Timestamp.now(),
                department = "Klinik Pakar Jantung",
                status = "Akan datang"
            ),
            reminders = listOf(
                Reminder(
                    medication = com.example.medialert.data.Medication(name = "Amoxicillin 250mg"),
                    dosage = "1",
                    unit = "capsule(s)",
                    remainingStock = 12,
                    times = listOf("08:00 AM")
                )
            ),
            onProfileClick = {},
            onContactClick = {},
            onTakenClick = {}
        )
    }
}
