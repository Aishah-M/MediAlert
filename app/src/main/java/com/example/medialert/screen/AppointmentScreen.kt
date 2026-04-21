package com.example.medialert.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medialert.data.Appointment
import com.example.medialert.theme.MediAlertTheme
import com.example.medialert.viewModel.AppointmentVM
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AppointmentScreen(
    viewModel: AppointmentVM = viewModel()
) {
    val appointments by viewModel.appointments
    val isLoading by viewModel.isLoading

    AppointmentContent(
        appointments = appointments,
        isLoading = isLoading
    )
}

@Composable
fun AppointmentContent(
    appointments: List<Appointment>,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(13.dp)
            ) {
                if (appointments.isEmpty()) {
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
}

@Composable
fun AppointmentInfoCard(appointment: Appointment) {
    // Format helpers
    val dateFull = appointment.timestamp?.toDate()?.let {
        SimpleDateFormat("dd/MM/yyyy  EEEE", Locale.forLanguageTag("ms-MY")).format(it).uppercase()
    } ?: "N/A"
    
    val timeOnly = appointment.timestamp?.toDate()?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it).uppercase()
    } ?: "N/A"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateFull,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeOnly,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                val isUpcoming = appointment.status == "Akan datang"
                Surface(
                    color = if (isUpcoming) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = appointment.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
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

            AppointmentDetailRow(label = "Jabatan", value = appointment.department)
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
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(100.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
fun AppointmentScreenEmptyPreview() {
    MediAlertTheme {
        AppointmentContent(appointments = emptyList(), isLoading = false)
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun AppointmentScreenLoadingPreview() {
    MediAlertTheme {
        AppointmentContent(appointments = emptyList(), isLoading = true)
    }
}

@Preview(showBackground = true, name = "With Data")
@Composable
fun AppointmentScreenDataPreview() {
    MediAlertTheme {
        AppointmentContent(
            appointments = listOf(
                Appointment(
                    id = "1",
                    timestamp = com.google.firebase.Timestamp.now(),
                    department = "Klinik Pakar Ortopedik",
                    reason = "Pemeriksaan Susulan",
                    status = "Akan datang"
                )
            ),
            isLoading = false
        )
    }
}
