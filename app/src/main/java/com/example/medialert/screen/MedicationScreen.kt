package com.example.medialert.screen

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.medialert.R
import com.example.medialert.theme.MediAlertTheme

// Data Class to match Firebase Structure
data class Medication(
    val imageUrl: String = "",
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",
    val purpose: String = "",
    val instructions: String = "",
    val prescriptionDate: String = "",
    val doctorName: String = ""
)

@Composable
fun MedicationScreen(
    medications: List<Medication> = emptyList(),
    //modifier: Modifier = Modifier
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Check if the list from API is empty
            if (medications.isEmpty()) {
                // EMPTY STATE UI
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

@Composable
fun MedicationInfoCard(medication: Medication) {
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
                contentDescription = "Medicine Image",
                placeholder = painterResource(R.drawable.mica2),
                error = painterResource(R.drawable.mica2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(MaterialTheme.shapes.large),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                // NEW: Row containing Name and Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically // Aligns name and button centers
                ) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f) // This pushes the button to the far right
                    )
                    Button(
                        onClick = { /* TODO: Logic to add this to your reminders */ },
                        modifier = Modifier.size(40.dp), // Set a square size for a centered look
                        shape = MaterialTheme.shapes.medium,
                        // Set contentPadding to 0 to ensure the icon is centered
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_add_24),
                            contentDescription = "Add Medication",
                            // Removed the end padding so it stays in the middle
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 5.dp),
                    thickness = 0.7.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Detailed Information
                MedicationDetailRow(label = "Dos", value = medication.dosage)
                MedicationDetailRow(label = "Kekerapan", value = medication.frequency)
                MedicationDetailRow(label = "Tempoh", value = medication.duration)
                MedicationDetailRow(label = "Tujuan", value = medication.purpose)
                MedicationDetailRow(label = "Arahan", value = medication.instructions)
                MedicationDetailRow(label = "Tarikh", value = medication.prescriptionDate)
                MedicationDetailRow(label = "Doktor", value = medication.doctorName)
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
        MedicationScreen(medications = emptyList())
    }
}

@Preview(showBackground = true, name = "With Data")
@Composable
fun MedicationScreenDataPreview() {
    MediAlertTheme {
        MedicationScreen(
            medications = listOf(
                Medication(
                    name = "Amoxicillin 250mg",
                    dosage = "1 Biji",
                    frequency = "3 kali sehari",
                    duration = "7 hari",
                    purpose = "Antibiotik",
                    instructions = "Selepas makan",
                    prescriptionDate = "14/03/2026",
                    doctorName = "Dr. Ahmad"
                )
            )
        )
    }
}