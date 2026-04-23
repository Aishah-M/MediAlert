package com.example.medialert.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.sp
import com.example.medialert.data.SampleData
import com.example.medialert.R
import com.example.medialert.data.Reminder
import com.example.medialert.theme.MediAlertTheme

@Composable
fun ReminderScreen(
    onAddClick: () -> Unit,
    onEditClick: (Reminder) -> Unit, // <--- MAKE SURE THIS LINE EXISTS
    reminders: List<Reminder>,      // <--- AND THIS MATCHES YOUR DATA CLASS
    modifier: Modifier = Modifier
) {
    // 1. Wrap everything in a Surface to set the screen background color
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) // Soft light grey background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Button "Add Medication"
            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .padding(vertical = 10.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_add_24),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = "Tambah peringatan ubat")
            }

            // --- LOGIC STARTS HERE ---
            if (reminders.isEmpty()) {
                // EMPTY STATE
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tiada peringatan ubat.\nSila tekan '+' untuk menambah peringatan",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp) // Extra space at the bottom
                ) {
                    items(10) { index ->
                        ReminderItemPlaceholder(index + 1)
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderItemPlaceholder(number: Int) {
    val isLowStock = number < 3

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            // 2. Keep the Card white so it stands out against the grey background
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp) // Increased shadow for better look
    ) {
        Row(
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Medicine Image
                Image(
                    painter = painterResource(id = R.drawable.mal),
                    contentDescription = "Medicine Image",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            MaterialTheme.shapes.medium
                        ),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Medication Name
                Text(
                    text = "Amoxicillin 250mg $number",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Times
                Text(
                    text = "⏰ 08:00 AM, 01:00 PM, 05:00 PM",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                // Dos
                Text(
                    text = "Dos: 1 kapsul",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Inventory Status
                Surface(
                    color = if (isLowStock) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Tinggal $number kapsul",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Button Column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    modifier = Modifier
                        .width(70.dp)
                        .height(32.dp),
                    onClick = { },
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Ubah", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                Button(
                    modifier = Modifier
                        .width(70.dp)
                        .height(32.dp),
                    onClick = { },
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Padam", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
fun ReminderScreenEmptyPreview() {
    MediAlertTheme {
        ReminderScreen(
            onAddClick = {},
            onEditClick = {}, // Added missing parameter
            reminders = emptyList()
        )
    }
}

@Preview(showBackground = true, name = "With Data")
@Composable
fun ReminderScreenDataPreview() {
    MediAlertTheme {
        ReminderScreen(
            onAddClick = {},
            onEditClick = {}, // Added missing parameter
            reminders = SampleData.medicationReminders // Used real Reminder list instead of Ints
        )
    }
}