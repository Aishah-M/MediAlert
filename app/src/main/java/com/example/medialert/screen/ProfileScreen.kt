package com.example.medialert.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medialert.R
import com.example.medialert.data.UserProfile
import com.example.medialert.theme.MediAlertTheme
import com.example.medialert.viewModel.ProfileVM

@Composable
fun ProfileScreen(
    viewModel: ProfileVM = viewModel(),
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.userProfile
    val isLoading by viewModel.isLoading

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        if (isLoading && user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            user?.let { profile ->
                ProfileContent(
                    user = profile,
                    onEditClick = onEditClick
                )
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Gagal memuat profil.")
            }
        }
    }
}

@Composable
fun ProfileContent(
    user: UserProfile,
    onEditClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    // Auto-calculate from IC
    val displayAge = user.calculateAgeFromIC()
    val displayBirthDate = user.calculateBirthDateFromIC()
    val displayGender = user.calculateGenderFromIC()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(13.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header Row (Logo and Edit Button)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.width(48.dp))

            // Profile Picture
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.person_24dp_000000),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Column to stack Icon and Text vertically
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable { onEditClick() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_edit_24),
                    contentDescription = "Edit Profile",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Ubah profil",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Maklumat Peribadi Card
        ProfileSectionCard(title = "Maklumat Peribadi") {
            ProfileDetailRow(label = "Nama penuh", value = user.fullName)
            ProfileDetailRow(label = "No. KP", value = user.icNumber)
            ProfileDetailRow(label = "Tarikh lahir", value = displayBirthDate)
            ProfileDetailRow(label = "Umur", value = "$displayAge Tahun")
            ProfileDetailRow(label = "Jantina", value = displayGender)
            ProfileDetailRow(label = "No. Telefon", value = user.phoneNumber)
            ProfileDetailRow(label = "Email", value = user.email)
            ProfileDetailRow(label = "Jenis darah", value = user.bloodType)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Maklumat Kesihatan Card
        ProfileSectionCard(title = "Maklumat Kesihatan") {
            ProfileDetailRow(label = "Penyakit kronik", user.chronicDiseases)
            ProfileDetailRow(label = "Alahan", value = user.allergies)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Hubungan Kecemasan Card
        ProfileSectionCard(title = "Kontak Kecemasan") {
            ProfileDetailRow(label = "Nama", value = user.emergencyContactName)
            ProfileDetailRow(label = "Hubungan", user.emergencyContactRelation)
            ProfileDetailRow(label = "No. Telefon", user.emergencyContactPhone)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ProfileSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            HorizontalDivider(thickness = 0.7.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MediAlertTheme {
        ProfileContent(
            user = UserProfile(
                fullName = "Bakri Bin Selamat",
                icNumber = "670706106067",
                phoneNumber = "012-3456789",
                email = "bakri@example.com"
            ),
            onEditClick = {}
        )
    }
}
