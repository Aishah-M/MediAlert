package com.example.medialert.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medialert.data.Reminder
import com.example.medialert.theme.MediAlertTheme
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditScreen(
    existingReminder: Reminder? = null,
    onSave: (Reminder) -> Unit,
    onCancel: () -> Unit
) {
    // Corrected state initialization using medicationName string
    var medName by remember { mutableStateOf(existingReminder?.medicationName ?: "") }
    
    var dosageValue by remember { mutableStateOf(existingReminder?.dosage ?: "") }
    var selectedUnit by remember { mutableStateOf(existingReminder?.unit ?: "") }
    var unitExpanded by remember { mutableStateOf(false) }
    val unitList = listOf( "biji", "sudu teh", "sudu besar", "sacet", "mililiter(ml)", "titis", "sapuan", "semburan", "sedutan", "keping")

    // Inventory States
    var totalInventory by remember { mutableStateOf(existingReminder?.totalStock?.toString() ?: "") }
    var remainingInventory by remember { mutableStateOf(existingReminder?.remainingStock?.toString() ?: "") }

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
            Text(
                text = if (existingReminder == null) "Tambah Peringatan Baru" else "Ubah Peringatan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Input: Nama Ubat
            OutlinedTextField(
                value = medName,
                onValueChange = { medName = it },
                label = { Text("Nama Ubat (Contoh: Vitamin C)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dosage and Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = dosageValue,
                    onValueChange = { if (it.all { char -> char.isDigit() }) dosageValue = it },
                    label = { Text("Dos") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded },
                    modifier = Modifier.weight(1.2f)
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

            // Inventory Section
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

            // Reminder Times
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
                }
            }

            Button(
                onClick = { showTimePicker = true },
                modifier = Modifier.padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Masa")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Batal")
                }
                Button(
                    onClick = {
                        val newReminder = (existingReminder ?: Reminder()).copy(
                            medicationName = medName,
                            dosage = dosageValue,
                            unit = selectedUnit,
                            totalStock = totalInventory.toIntOrNull() ?: 0,
                            remainingStock = remainingInventory.toIntOrNull() ?: 0,
                            times = reminderTimes
                        )
                        onSave(newReminder)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = medName.isNotBlank() && reminderTimes.isNotEmpty()
                ) {
                    Text("Simpan")
                }
            }
        }
    }

    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val hour = if (timePickerState.hour == 0 || timePickerState.hour == 12) 12 else timePickerState.hour % 12
                    val amPm = if (timePickerState.hour < 12) "AM" else "PM"
                    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour, timePickerState.minute, amPm)
                    
                    if (!reminderTimes.contains(formattedTime)) {
                        reminderTimes = (reminderTimes + formattedTime).sorted()
                    }
                    showTimePicker = false
                }) { Text("Tambah") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Batal") }
            },
            title = { Text("Pilih Masa Peringatan") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReminderEditScreenPreview() {
    MediAlertTheme {
        ReminderEditScreen(onSave = {}, onCancel = {})
    }
}
