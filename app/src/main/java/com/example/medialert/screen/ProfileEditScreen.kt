package com.example.medialert.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medialert.data.UserProfile
import com.example.medialert.theme.MediAlertTheme

@Composable
fun ProfileEditScreen(
    user: UserProfile = com.example.medialert.data.SampleData.userProfile,
    onSaveClick: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    // State for each input field
    var fullName by remember { mutableStateOf(user.fullName) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber) }
    var email by remember { mutableStateOf(user.email) }
    var icNumber by remember { mutableStateOf(user.icNumber) }

    // New Health Info State
    var chronicDiseases by remember { mutableStateOf(user.chronicDiseases) }
    var allergies by remember { mutableStateOf(user.allergies) }

    var emergencyName by remember { mutableStateOf(user.emergencyContactName) }
    var emergencyRelation by remember { mutableStateOf(user.emergencyContactRelation) }
    var emergencyPhone by remember { mutableStateOf(user.emergencyContactPhone) }


    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Personal Information Section ---
        EditSectionTitle(title = "Maklumat Peribadi")

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Nama Penuh") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            readOnly = true, // User cannot edit
            enabled = false  // Greys out the field to indicate it's locked
        )

        OutlinedTextField(
            value = icNumber,
            onValueChange = { icNumber = it },
            label = { Text("No. Kad Pengenalan") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            readOnly = true, // User cannot edit
            enabled = false  // Greys out the field to indicate it's locked
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("No. Telefon") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // --- Maklumat Kesihatan Section ---
        EditSectionTitle(title = "Maklumat Kesihatan")

        OutlinedTextField(
            value = chronicDiseases,
            onValueChange = { chronicDiseases = it },
            label = { Text("Penyakit Kronik") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = allergies,
            onValueChange = { allergies = it },
            label = { Text("Alahan") },
            modifier = Modifier.fillMaxWidth()
        )

        // --- Emergency Contact Section ---
        EditSectionTitle(title = "Hubungan Kecemasan")

        OutlinedTextField(
            value = emergencyName,
            onValueChange = { emergencyName = it },
            label = { Text("Nama Waris/Kenalan") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = emergencyRelation,
            onValueChange = { emergencyRelation = it },
            label = { Text("Hubungan") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = emergencyPhone,
            onValueChange = { emergencyPhone = it },
            label = { Text("No. Telefon Kecemasan") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(3.dp))

        // --- Save Button ---
        Button(
            onClick = {
                val updatedUser = user.copy(
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    email = email,
                    icNumber = icNumber,
                    emergencyContactName = emergencyName,
                    emergencyContactPhone = emergencyPhone
                )
                onSaveClick(updatedUser)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Simpan", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun EditSectionTitle(title: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(modifier = Modifier.padding(top = 2.dp), thickness = 1.dp)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileEditScreenPreview() {
    MediAlertTheme {
        ProfileEditScreen(onSaveClick = {})
    }
}