package com.example.medialert.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medialert.R
import com.example.medialert.data.Medication
import com.example.medialert.data.Reminder
import com.example.medialert.data.SampleData
import com.example.medialert.theme.MediAlertTheme
import kotlin.text.padStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditScreen(
    // Receive existing data if editing, or null if adding new
    existingReminder: Reminder? = null,
    onSave: (Reminder) -> Unit,
    onCancel: () -> Unit
) {
    // --- States initialized from the Reminder data class ---
    var selectedMed by remember { mutableStateOf(existingReminder?.medication) }
    var medNameExpanded by remember { mutableStateOf(false) }

    var dosageValue by remember { mutableStateOf(existingReminder?.dosage ?: "") }

    var selectedUnit by remember { mutableStateOf(existingReminder?.unit ?: "") }
    var unitExpanded by remember { mutableStateOf(false) }
    val unitList = listOf(
        "ampoule(s)", "capsule(s)", "drop()", "gram(s)",
        "injection(s)", "miligram(s)", "mililiter(s)", "mm",
        "pill(s)", "sachet(s)", "spray(s)", "tablespoon(s)",
        "teaspoon(s)", "piece(s)", "patch(es)"
    )

    var totalInventory by remember { mutableStateOf(existingReminder?.totalStock ?: "") }
    var remainingInventory by remember { mutableStateOf(existingReminder?.remainingStock ?: "") }

    var reminderTimes by remember { mutableStateOf(existingReminder?.times ?: emptyList()) }
    var showTimePicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(15.dp)
                .verticalScroll(scrollState)
        ) {
            // 1. Dropdown: Nama Ubat
            ExposedDropdownMenuBox(
                expanded = medNameExpanded,
                onExpandedChange = { medNameExpanded = !medNameExpanded }
            ) {
                OutlinedTextField(
                    value = selectedMed?.name?:"",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pilih Nama Ubat") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = medNameExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = medNameExpanded,
                    onDismissRequest = { medNameExpanded = false }
                ) {
                    SampleData.availableMedications.forEach { med ->
                        DropdownMenuItem(
                            text = { Text(med.name) },
                            onClick = {
                                selectedMed = med
                                medNameExpanded = false
                            }
                        )
                    }
                }
            }
            // --- IMAGE DISPLAY LOGIC ---
            // If a medicine is selected, show the image right below the dropdown
            selectedMed?.imageRes?.let { data ->
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = painterResource(id = data),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            MaterialTheme.shapes.medium
                        ),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Row containing Dosage and Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dos Diambil (Numeric Input)
                OutlinedTextField(
                    value = dosageValue,
                    onValueChange = { if (it.all { char -> char.isDigit() }) dosageValue = it},
                    label = { Text("Dos diambil") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // Unit (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        unitList.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    selectedUnit = unit
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Inventori Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = totalInventory,
                    onValueChange = { if (it.all { char -> char.isDigit() }) totalInventory = it },
                    label = { Text("Jumlah Asal") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = remainingInventory,
                    onValueChange = { if (it.all { char -> char.isDigit() }) remainingInventory = it },
                    label = { Text("Baki Semasa") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Masa Peringatan Section
            Text(
                text = "Masa Peringatan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            reminderTimes.forEachIndexed { index, time ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(text = time, modifier = Modifier.padding(12.dp))
                    }
                    IconButton(onClick = {
                        reminderTimes = reminderTimes.toMutableList().apply { removeAt(index) }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Padam", tint = MaterialTheme.colorScheme.error)
                    }
//                    IconButton(onClick = { reminderTimes = reminderTimes.filterIndexed { i, _ -> i != index } }) {
//                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
//                    }
                }
            }

            Button(
                onClick = { showTimePicker = true },
                modifier = Modifier.padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Masa")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        // Reset all states
                        selectedMed = null; selectedUnit = ""; totalInventory = ""
                        remainingInventory = ""; reminderTimes = emptyList()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Set Semula")
                }
                Button(
                    onClick = { /* TODO: Save to Room/LocalStorage */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Simpan")
                }
            }
        }
    }
    /*
    // Time Picker Dialog logic
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formattedTime = "${timePickerState.hour}:${timePickerState.minute.toString().padStart(2, '0')}"
                    reminderTimes = reminderTimes + formattedTime
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Batal") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
     */
    // --- CORRECTED TIME PICKER DIALOG ---
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            is24Hour = false // This makes the Picker UI show AM/PM buttons
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // Logic to convert 24h data to 12h display string
                    val hour = timePickerState.hour
                    val minute = timePickerState.minute.toString().padStart(2, '0')
                    val amPm = if (hour < 12) "AM" else "PM"

                    val displayHour = when {
                        hour == 0 -> 12
                        hour > 12 -> hour - 12
                        else -> hour
                    }

                    val formattedTime = "$displayHour:$minute $amPm"

                    reminderTimes = reminderTimes + formattedTime
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Batal") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
fun ReminderEditEmptyPreview() {
    MediAlertTheme {
        ReminderEditScreen(
            existingReminder = null, // null means "Add New" mode
            onSave = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReminderEditPreview() {
    MediAlertTheme {
        // Test with existing sample data
        ReminderEditScreen(
            existingReminder = SampleData.medicationReminders[0],
            onSave = {},
            onCancel = {}
        )
    }
}