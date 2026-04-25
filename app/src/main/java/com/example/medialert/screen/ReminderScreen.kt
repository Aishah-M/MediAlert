package com.example.medialert.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.medialert.R
import com.example.medialert.data.Reminder
import com.example.medialert.theme.MediAlertTheme

@Composable
fun ReminderScreen(
    onAddClick: () -> Unit,
    onEditClick: (Reminder) -> Unit,
    onDeleteClick: (Reminder) -> Unit,
    reminders: List<Reminder>,
    modifier: Modifier = Modifier
) {
    var reminderToDelete by remember { mutableStateOf<Reminder?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 13.dp)
                .padding(bottom = 13.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onAddClick,
                modifier = Modifier.padding(vertical = 10.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_add_24),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = "Tambah peringatan ubat")
            }

            if (reminders.isEmpty()) {
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
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(reminders) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            onEdit = { onEditClick(reminder) },
                            onDelete = { reminderToDelete = reminder }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (reminderToDelete != null) {
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        reminderToDelete?.let { onDeleteClick(it) }
                        reminderToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Padam", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderToDelete = null }) {
                    Text("Batal")
                }
            },
            title = { Text("Padam Peringatan?") },
            text = { Text("Adakah anda pasti ingin memadam peringatan untuk ${reminderToDelete?.medicationName}?") }
        )
    }
}

@Composable
fun ReminderItem(reminder: Reminder, onEdit: () -> Unit, onDelete: () -> Unit) {
    val isLowStock = reminder.remainingStock < 5

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(15.dp).fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Image(
                    painter = painterResource(id = R.drawable.mal),
                    contentDescription = null,
                    modifier = Modifier.size(70.dp).clip(MaterialTheme.shapes.medium).border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = reminder.medicationName.ifEmpty { "Ubat" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text(text = "⏰ ${reminder.times.joinToString(", ")}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Dos: ${reminder.dosage} ${reminder.unit}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = if (isLowStock) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Tinggal ${reminder.remainingStock} ${reminder.unit}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onEdit, modifier = Modifier.width(75.dp).height(32.dp), shape = MaterialTheme.shapes.medium, contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                    Text("Ubah", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Button(onClick = onDelete, modifier = Modifier.width(75.dp).height(32.dp), shape = MaterialTheme.shapes.medium, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error), contentPadding = PaddingValues(0.dp)) {
                    Text("Padam", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReminderScreenPreview() {
    MediAlertTheme {
        ReminderScreen(
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            reminders = listOf(
                Reminder(
                    medicationName = "Vitamin C",
                    dosage = "1",
                    unit = "tablet",
                    remainingStock = 10,
                    times = listOf("08:00 AM", "08:00 PM")
                ),
                Reminder(
                    medicationName = "Fish Oil",
                    dosage = "2",
                    unit = "capsule(s)",
                    remainingStock = 3,
                    times = listOf("12:00 PM")
                )
            )
        )
    }
}
