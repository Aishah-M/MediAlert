package com.example.medialert.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medialert.data.UserProfile
import com.example.medialert.theme.MediAlertTheme
import com.example.medialert.viewModel.ProfileVM

@Composable
fun ProfileEditScreen(
    viewModel: ProfileVM = viewModel(),
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.userProfile
    val isLoading by viewModel.isLoading

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isLoading && user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            user?.let { profile ->
                ProfileEditContent(
                    user = profile,
                    onSave = { updatedProfile ->
                        viewModel.updateUserProfile(updatedProfile, onSaveClick)
                    }
                )
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Gagal memuat profil.")
            }
        }
    }
}

@Composable
fun ProfileEditContent(
    user: UserProfile,
    onSave: (UserProfile) -> Unit
) {
    // State for each input field initialized with user data
    var phoneNumber by remember { mutableStateOf(user.phoneNumber) }
    var email by remember { mutableStateOf(user.email) }
    
    var bloodType by remember { mutableStateOf(user.bloodType) }
    
    // Health Info State
    var chronicDiseases by remember { mutableStateOf(user.chronicDiseases) }
    var allergies by remember { mutableStateOf(user.allergies) }

    // Emergency Contact State
    var emergencyName by remember { mutableStateOf(user.emergencyContactName) }
    var emergencyRelation by remember { mutableStateOf(user.emergencyContactRelation) }
    var emergencyPhone by remember { mutableStateOf(user.emergencyContactPhone) }

    // Auto calculate birthdate, age, and gender from IC
    val birthDate = remember(user.icNumber) { user.calculateBirthDateFromIC() }
    val age = remember(user.icNumber) { user.calculateAgeFromIC() }
    val gender = remember(user.icNumber) { user.calculateGenderFromIC() }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EditSectionTitle(title = "Maklumat Peribadi")

        // Locked Fields with a slightly darker background for better visibility
        ReadOnlyLockedField(
            value = user.fullName,
            label = "Nama Penuh",
            modifier = Modifier.fillMaxWidth()
        )

        ReadOnlyLockedField(
            value = user.icNumber,
            label = "No. Kad Pengenalan",
            modifier = Modifier.fillMaxWidth()
        )

        // Locked Fields (Calculated from IC)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReadOnlyLockedField(
                value = birthDate,
                label = "Tarikh Lahir",
                modifier = Modifier.weight(1.5f)
            )
            ReadOnlyLockedField(
                value = "$age Tahun",
                label = "Umur",
                modifier = Modifier.weight(1f)
            )
        }

        ReadOnlyLockedField(
            value = gender,
            label = "Jantina",
            modifier = Modifier.fillMaxWidth()
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

        OutlinedTextField(
            value = bloodType,
            onValueChange = { bloodType = it },
            label = { Text("Jenis Darah") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

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

        EditSectionTitle(title = "Hubungan Kecemasan")

        OutlinedTextField(
            value = emergencyName,
            onValueChange = { emergencyName = it },
            label = { Text("Nama Waris/Kenalan") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = emergencyRelation,
            onValueChange = { emergencyRelation = it },
            label = { Text("Hubungan") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = emergencyPhone,
            onValueChange = { emergencyPhone = it },
            label = { Text("No. Telefon Kecemasan") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val updatedProfile = user.copy(
                    phoneNumber = phoneNumber,
                    email = email,
                    birthDate = birthDate,
                    age = age,
                    gender = gender,
                    bloodType = bloodType,
                    chronicDiseases = chronicDiseases,
                    allergies = allergies,
                    emergencyContactName = emergencyName,
                    emergencyContactRelation = emergencyRelation,
                    emergencyContactPhone = emergencyPhone
                )
                onSave(updatedProfile)
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

/**
 * A custom OutlinedTextField for read-only fields that look "nicer" and slightly darker.
 */
@Composable
fun ReadOnlyLockedField(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = { Text(label) },
        modifier = modifier,
        readOnly = true,
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) // Darker greyish background
        )
    )
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
        ProfileEditContent(
            user = UserProfile(
                fullName = "Bakri Bin Selamat",
                icNumber = "670706106067",
                phoneNumber = "012-3456789",
                email = "bakri@example.com"
            ),
            onSave = {}
        )
    }
}
