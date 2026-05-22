package com.example.medialert.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medialert.R
import com.example.medialert.viewModel.LoginVM

@Composable
fun LoginScreen(
    viewModel: LoginVM = viewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var icNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    
    val errorMessage by viewModel.errorMessage
    val successMessage by viewModel.successMessage
    val isLoading by viewModel.isLoading

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { 
                showForgotDialog = false
                viewModel.clearMessages()
            },
            title = { Text("Lupa Kata Laluan?") },
            text = {
                Column {
                    Text("Sila masukkan alamat e-mel anda untuk menerima pautan set semula kata laluan.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { 
                            forgotEmail = it 
                            if (errorMessage != null) viewModel.clearError()
                        },
                        label = { Text("E-mel") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMessage != null
                    )
                    if (errorMessage != null && !isLoading) {
                        Text(text = errorMessage!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                    if (successMessage != null) {
                        Text(text = successMessage!!, color = Color(0xFF4CAF50), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetPassword(forgotEmail) },
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text("Hantar")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showForgotDialog = false
                    viewModel.clearMessages()
                }) {
                    Text("Batal")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.mal),
                contentDescription = "App Logo",
                modifier = Modifier.size(150.dp)
            )
            Text(
                text = "MediAlert",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Error Message Display
            if (errorMessage != null && !showForgotDialog) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            OutlinedTextField(
                value = icNumber,
                onValueChange = { 
                    if (it.length <= 12) icNumber = it 
                    if (errorMessage != null) viewModel.clearError()
                },
                label = { Text("No. Kad Pengenalan (IC)") },
                placeholder = { Text("Contoh: 670330100178") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(painterResource(id = R.drawable.person_24dp_000000), contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = errorMessage != null && !showForgotDialog
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it 
                    if (errorMessage != null) viewModel.clearError()
                },
                label = { Text("Kata Laluan") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = errorMessage != null && !showForgotDialog,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(id = if (passwordVisible) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24),
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            TextButton(
                onClick = { 
                    viewModel.clearMessages()
                    forgotEmail = ""
                    showForgotDialog = true 
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Lupa Kata Laluan?", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.loginPatient(icNumber, password, onLoginSuccess)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("LOG MASUK", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Belum mempunyai akaun?", color = MaterialTheme.colorScheme.outline)
                TextButton(onClick = onNavigateToRegister) {
                    Text("Daftar Sekarang", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    com.example.medialert.theme.MediAlertTheme {
        LoginScreen(onLoginSuccess = {}, onNavigateToRegister = {})
    }
}
