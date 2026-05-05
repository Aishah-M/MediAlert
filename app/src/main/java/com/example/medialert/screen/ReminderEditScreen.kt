package com.example.medialert.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medialert.data.Reminder
import com.example.medialert.theme.MediAlertTheme
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditScreen(
    existingReminder: Reminder? = null,
    onSave: (Reminder) -> Unit,
    onCancel: () -> Unit
) {
    var medName by remember { mutableStateOf(existingReminder?.medicationName ?: "") }
    var dosageValue by remember { mutableStateOf(existingReminder?.dosage ?: "") }
    var selectedUnit by remember { mutableStateOf(existingReminder?.unit ?: "") }
    var unitExpanded by remember { mutableStateOf(false) }
    val unitList = listOf( "biji", "sudu teh", "sudu besar", "sacet", "mililiter(ml)", "titis", "sapuan", "semburan", "sedutan", "keping")

    var totalInventory by remember { mutableStateOf(existingReminder?.totalStock?.toString() ?: "") }
    var remainingInventory by remember { mutableStateOf(existingReminder?.remainingStock?.toString() ?: "") }

    var reminderTimes by remember { mutableStateOf(existingReminder?.times ?: emptyList()) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Scheduling states
    var startDate by remember { mutableStateOf(existingReminder?.startDate) }
    var endDate by remember { mutableStateOf(existingReminder?.endDate) }
    var untilFinish by remember { mutableStateOf(existingReminder?.untilFinish ?: false) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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

            OutlinedTextField(
                value = medName,
                onValueChange = { medName = it },
                label = { Text("Nama Ubat (Contoh: Vitamin C)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            // --- DATE RANGE SECTION ---
            Text(
                text = "Tempoh Pengambilan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = startDate?.toDate()?.let { sdf.format(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Tarikh Mula") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showStartDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pilih Tarikh")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = untilFinish,
                    onCheckedChange = { untilFinish = it }
                )
                Text(text = "Sehingga habis ubat", style = MaterialTheme.typography.bodyMedium)
            }

            if (!untilFinish) {
                OutlinedTextField(
                    value = endDate?.toDate()?.let { sdf.format(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tarikh Tamat") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pilih Tarikh")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- TIME PICKER SECTION ---
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
                            times = reminderTimes,
                            startDate = startDate,
                            endDate = if (untilFinish) null else endDate,
                            untilFinish = untilFinish
                        )
                        onSave(newReminder)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = medName.isNotBlank() && reminderTimes.isNotEmpty() && startDate != null
                ) {
                    Text("Simpan")
                }
            }
        }
    }

    // --- DIALOGS ---

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        startDate = Timestamp(Date(it))
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        endDate = Timestamp(Date(it))
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
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
