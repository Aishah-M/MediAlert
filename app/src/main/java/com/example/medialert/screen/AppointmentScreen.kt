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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medialert.data.Appointment
import com.example.medialert.theme.MediAlertTheme

@Composable
fun AppointmentScreen(
    appointments: List<Appointment> = emptyList()

    /* untuk test bila ada data
    appointments: List<Appointment> = listOf(
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
    */
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp)
        ) {
            // Check if there are any appointments
            if (appointments.isEmpty()) {
                // EMPTY STATE UI
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tiada janji temu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Maklumat janji temu akan dipaparkan di sini setelah dikemaskini.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                    )
                }
            } else {
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
            modifier = Modifier.padding(15.dp)
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

@Preview(showBackground = true, name = "Empty State")
@Composable
fun AppointmentScreenEmptyPreview() {
    MediAlertTheme {
        AppointmentScreen(appointments = emptyList())
    }
}

@Preview(showBackground = true, name = "With Data")
@Composable
fun AppointmentScreenDataPreview() {
    MediAlertTheme {
        AppointmentScreen(
            appointments = listOf(
                Appointment(
                    date = "16/3/2026",
                    day = "MONDAY",
                    time = "9:00 AM",
                    department = "Klinik Pakar Pembedahan Am (SOPD)",
                    hospital = "Hospital Tanjung Karang",
                    reason = "Pemeriksaan berkala",
                    status = "Akan datang"
                )
            )
        )
    }
}