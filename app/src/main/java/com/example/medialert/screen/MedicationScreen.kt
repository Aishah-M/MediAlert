package com.example.medialert.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.medialert.R
import com.example.medialert.data.Medication
import com.example.medialert.theme.MediAlertTheme
import com.example.medialert.viewModel.MedicationVM
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MedicationScreen(
    viewModel: MedicationVM = viewModel()
) {
    val medications by viewModel.medications
    val isLoading by viewModel.isLoading

    MedicationContent(
        medications = medications,
        isLoading = isLoading
    )
}

@Composable
fun MedicationContent(
    medications: List<Medication>,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(13.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (medications.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tiada ubat",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Senarai ubat akan dipaparkan di sini setelah dikemaskini.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(medications) { medication ->
                            MedicationInfoCard(medication)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationInfoCard(medication: Medication) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val startStr = medication.startDate?.toDate()?.let { sdf.format(it) } ?: ""
    val endStr = medication.endDate?.toDate()?.let { sdf.format(it) } ?: ""
    val prescriptionDateStr = medication.prescriptionDate?.toDate()?.let { sdf.format(it) } ?: ""
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Full width image
            AsyncImage(
                model = medication.imageUrl,
                contentDescription = "Medicine Image: ${medication.name}",
                placeholder = painterResource(R.drawable.mica),
                error = painterResource(R.drawable.mica),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(MaterialTheme.shapes.large),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = medication.name.ifEmpty { "Nama Ubat" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (medication.unit.isNotEmpty()) {
                            Text(
                                text = "Unit: ${medication.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Button(
                        onClick = { /* TODO: Logic to add this to your reminders */ },
                        modifier = Modifier.size(36.dp),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_add_24),
                            contentDescription = "Add Medication",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 5.dp),
                    thickness = 0.7.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Detailed prescription info added by doctor
                if (medication.dosage.isNotEmpty()) MedicationDetailRow(label = "Dos", value = medication.dosage)
                if (medication.frequency.isNotEmpty()) MedicationDetailRow(label = "Kekerapan", value = medication.frequency)
                
                // New Time Field
                if (medication.times.isNotEmpty()) {
                    MedicationDetailRow(label = "Waktu", value = medication.times.joinToString(", "))
                }

                if (medication.duration.isNotEmpty()) MedicationDetailRow(label = "Tempoh", value = medication.duration)
                
                // New Date Range Field
                if (startStr.isNotEmpty()) {
                    val dateValue = if (medication.untilFinish) "$startStr sehingga habis" else if (endStr.isNotEmpty()) "$startStr - $endStr" else startStr
                    MedicationDetailRow(label = "Tarikh", value = dateValue)
                }

                if (medication.purpose.isNotEmpty()) MedicationDetailRow(label = "Tujuan", value = medication.purpose)
                if (medication.instruction.isNotEmpty()) MedicationDetailRow(label = "Arahan", value = medication.instruction)
                if (prescriptionDateStr.isNotEmpty()) MedicationDetailRow(label = "Tarikh Janji", value = prescriptionDateStr)
                if (medication.doctorName.isNotEmpty()) MedicationDetailRow(label = "Doktor", value = medication.doctorName)
            }
        }
    }
}

@Composable
fun MedicationDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
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
fun MedicationScreenEmptyPreview() {
    MediAlertTheme {
        MedicationContent(medications = emptyList(), isLoading = false)
    }
}

@Preview(showBackground = true, name = "With Data")
@Composable
fun MedicationScreenDataPreview() {
    MediAlertTheme {
        MedicationContent(
            medications = listOf(
                Medication(
                    id = "amlodipine_5mg",
                    name = "amlodipine 5mg",
                    unit = "tablet",
                    dosage = "1",
                    frequency = "Sekali sehari",
                    times = listOf("09:00 AM"),
                    duration = "30 hari",
                    purpose = "Darah Tinggi",
                    instruction = "Ambil selepas makan",
                    doctorName = "Dr. Aminah"
                )
            ),
            isLoading = false
        )
    }
}
