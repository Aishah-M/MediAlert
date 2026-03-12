package com.example.medialert.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medialert.theme.MediAlertTheme

// Data Class for Appointment
data class Appointment(
    val date: String = "",
    val day: String = "",
    val time: String = "",
    val department: String = "",
    val hospital: String = "",
    val reason: String = "",
    val status: String = "" // "Akan datang" or "Selesai"
)

@Composable
fun AppointmentScreen(
    modifier: Modifier = Modifier,
    // Sample data list
    appointments: List<Appointment> = listOf(
        Appointment(
            date = "16/3/2026",
            day = "MONDAY",
            time = "9:00 AM",
            department = "Klinik Pakar Pembedahan Am (SOPD)",
            hospital = "Hospital Tanjung Karang",
            reason = "Pemeriksaan berkala",
            status = "Akan datang"
        ),
        Appointment(
            date = "10/2/2026",
            day = "TUESDAY",
            time = "11:30 AM",
            department = "Jabatan Ortopedik",
            hospital = "Hospital Tanjung Karang",
            reason = "Rawatan susulan",
            status = "Selesai"
        ),
    )
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            /*
            Text(
                text = "Temujanji Saya",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
             */

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(appointments) { appointment ->
                    AppointmentInfoCard(appointment)
                }
            }
        }
    }
}

@Composable
fun AppointmentInfoCard(appointment: Appointment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(13.dp)
        ) {
            // Header Row: Date/Day and Status Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${appointment.date}  ${appointment.day}",
                        style = MaterialTheme.typography.titleMedium, //bodyLarge
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = appointment.time,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Status Label logic
                val isUpcoming = appointment.status == "Akan datang"
                Surface(
                    color = if (isUpcoming) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = appointment.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium, //labelLarge
                        fontWeight = FontWeight.Bold,
                        color = if (isUpcoming) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 7.dp),
                thickness = 0.7.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Details Section
            AppointmentDetailRow(label = "Jabatan", value = appointment.department)
            AppointmentDetailRow(label = "Hospital", value = appointment.hospital)
            AppointmentDetailRow(label = "Tujuan", value = appointment.reason)
        }
    }
}

@Composable
fun AppointmentDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label : ",
            style = MaterialTheme.typography.bodyMedium, //bodyLarge
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(100.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium, //bodyLarge
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppointmentScreenPreview() {
    MediAlertTheme {
        AppointmentScreen()
    }
}
