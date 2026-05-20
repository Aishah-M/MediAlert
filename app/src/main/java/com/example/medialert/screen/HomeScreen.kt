package com.example.medialert.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.medialert.viewModel.HomeVM
import com.example.medialert.viewModel.ReminderVM
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Data class to represent a specific dose at a specific time
data class MedicationTask(
    val reminder: Reminder,
    val time: String,
    val isTaken: Boolean,
    val dateStr: String,
    val taskMinutes: Int // Total minutes from midnight
)

@Composable
fun HomeScreen(
    homeViewModel: HomeVM = viewModel(),
    appointmentViewModel: AppointmentVM = viewModel(),
    reminderViewModel: ReminderVM = viewModel(),
    onProfileClick: () -> Unit,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by homeViewModel.userProfile
    val appointments by appointmentViewModel.appointments
    val reminders by reminderViewModel.reminders.collectAsState()
    val isLoading by homeViewModel.isLoading

    // Current time in minutes from midnight to track task availability
    var currentMinutes by remember {
        mutableStateOf(Calendar.getInstance().let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) })
    }

    // Refresh current time every 15 seconds to ensure lists update promptly
    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            delay(15000)
        }
    }
    
    // Create flattened list of tasks for today (12:00 AM - 11:59 PM)
    val todayTasks = remember(reminders) {
        val calendar = Calendar.getInstance()
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val endOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        
        reminders.flatMap { reminder ->
            val start = reminder.startDate ?: 0L
            val end = reminder.endDate ?: Long.MAX_VALUE
            
            // Check if active today
            val isAfterStart = start <= endOfToday
            val isBeforeEnd = reminder.untilFinish || end >= startOfToday
            
            if (isAfterStart && isBeforeEnd) {
                reminder.times.map { timeStr ->
                    val taskKey = "${dateStr}_$timeStr"
                    
                    // Parse time to minutes for comparison
                    val parts = timeStr.split("[: ]".toRegex())
                    var h = try { parts[0].toInt() } catch(e: Exception) { 0 }
                    val m = try { parts[1].toInt() } catch(e: Exception) { 0 }
                    val amPm = parts.last().uppercase()
                    if (amPm == "PM" && h < 12) h += 12
                    if (amPm == "AM" && h == 12) h = 0
                    val minutes = h * 60 + m

                    MedicationTask(
                        reminder = reminder,
                        time = timeStr,
                        isTaken = reminder.takenLog.contains(taskKey),
                        dateStr = dateStr,
                        taskMinutes = minutes
                    )
                }
            } else {
                emptyList()
            }
        }.sortedBy { it.taskMinutes }
    }

    // Filter tasks that should be visible based on current time (scheduled time <= current time)
    val visibleTasks = remember(todayTasks, currentMinutes) {
        todayTasks.filter { it.taskMinutes <= currentMinutes }
    }

    val nextAppointment = appointments.find { it.status == "Akan datang" }

    if (isLoading && user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        HomeContent(
            user = user ?: UserProfile(fullName = "Pengguna"),
            nextAppointment = nextAppointment,
            tasks = visibleTasks,
            allTasksToday = todayTasks,
            onProfileClick = onProfileClick,
            onContactClick = onContactClick,
            onTaskToggle = { task ->
                val updatedReminder = task.reminder.toggleTakenAt(task.dateStr, task.time)
                reminderViewModel.saveReminder(updatedReminder)
            },
            modifier = modifier
        )
    }
}

@Composable
fun HomeContent(
    user: UserProfile,
    nextAppointment: Appointment?,
    tasks: List<MedicationTask>,
    allTasksToday: List<MedicationTask>,
    onProfileClick: () -> Unit,
    onContactClick: () -> Unit,
    onTaskToggle: (MedicationTask) -> Unit,
    modifier: Modifier = Modifier
) {
    // Show Syabas only if ALL tasks scheduled for today are taken
    val allTaken = allTasksToday.isNotEmpty() && allTasksToday.all { it.isTaken }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        HomeHeader(user, onProfileClick)

        Column(modifier = Modifier.padding(13.dp)) {
            AppointmentCountdownCard(nextAppointment)

            Spacer(modifier = Modifier.height(24.dp))

            if (allTasksToday.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        Text(
                            text = "Jadual Ubat Hari Ini",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (tasks.isEmpty() && !allTaken) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Jadual ubat seterusnya akan muncul mengikut masa yang ditetapkan.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(tasks) { task ->
                            MedicationTaskCard(
                                task = task,
                                onTakenClick = { onTaskToggle(task) }
                            )
                        }
                    }
                    
                    if (allTaken) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Syabas! Semua ubat hari ini telah diambil.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = "Tiada jadual ubat untuk hari ini.", color = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Hai,", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontSize = 14.sp)
                Text(text = user.fullName, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                val displayAge = user.calculateAgeFromIC()
                val displayGender = user.calculateGenderFromIC()
                Text(text = "$displayAge Tahun • $displayGender", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f), fontSize = 12.sp)
            }
            Box(
                modifier = Modifier.size(55.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)).clickable { onProfileClick() },
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
            val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.time
            val diff = apptDate.time - today.time
            val days = diff / (1000 * 60 * 60 * 24)
            if (days > 0) days else 0L
        } ?: 0L
    }
    val dateStr = appointment.timestamp?.toDate()?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "N/A"
    val timeStr = appointment.timestamp?.toDate()?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it).uppercase() } ?: "N/A"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Temujanji Seterusnya", style = MaterialTheme.typography.titleMedium)
                Text(appointment.department, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("$dateStr • $timeStr", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(daysRemaining.toString(), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text("HARI LAGI", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MedicationTaskCard(task: MedicationTask, onTakenClick: () -> Unit) {
    val isTaken = task.isTaken
    val reminder = task.reminder
    
    Card(
        modifier = Modifier.fillMaxWidth().alpha(if (isTaken) 0.6f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = if (isTaken) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isTaken) 0.dp else 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.medicationName.ifEmpty { "Ubat" },
                    fontWeight = FontWeight.Bold,
                    color = if (isTaken) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${reminder.dosage} ${reminder.unit} • ${task.time}",
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
            
            Surface(
                color = if (isTaken) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.clickable(enabled = !isTaken) { onTakenClick() }
            ) {
                Text(
                    text = if (isTaken) "Telah diambil" else "Belum ambil",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isTaken) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LargeEmergencyButton(onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary), shape = RoundedCornerShape(12.dp)) {
        Icon(painter = painterResource(id = R.drawable.baseline_call_24), contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text("HUBUNGI FASILITI KESIHATAN", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreviewWithData() {
    val reminder = Reminder(medicationName = "Vitamin C", dosage = "1", unit = "biji", times = listOf("08:00 AM", "08:00 PM"))
    val tasks = listOf(
        MedicationTask(reminder, "08:00 AM", true, "2023-10-10", 480),
        MedicationTask(reminder, "08:00 PM", false, "2023-10-10", 1200)
    )
    com.example.medialert.theme.MediAlertTheme {
        HomeContent(
            user = UserProfile(fullName = "Siti Aminah", icNumber = "650330105432"),
            nextAppointment = Appointment(timestamp = com.google.firebase.Timestamp.now(), department = "Klinik Pakar", status = "Akan datang"),
            tasks = tasks,
            allTasksToday = tasks,
            onProfileClick = {}, onContactClick = {}, onTaskToggle = {}
        )
    }
}
