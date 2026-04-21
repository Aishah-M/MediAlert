package com.example.medialert

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.medialert.screen.AppointmentScreen
import com.example.medialert.screen.ContactScreen
import com.example.medialert.screen.HomeScreen
import com.example.medialert.screen.LoginScreen
import com.example.medialert.screen.RegisterScreen
import com.example.medialert.screen.MedicationScreen
import com.example.medialert.screen.ProfileEditScreen
import com.example.medialert.screen.ProfileScreen
import com.example.medialert.screen.ReminderEditScreen
import com.example.medialert.screen.ReminderScreen
import com.example.medialert.theme.MediAlertTheme
import com.example.medialert.data.SampleData
import com.google.firebase.auth.FirebaseAuth

enum class MediAlertScreen(@StringRes val title: Int) {
    Login(R.string.login),
    Register(R.string.signup),
    Home(R.string.home),
    Appointment(R.string.appointment),
    Medication(R.string.medication),
    Reminder(R.string.reminder),
    Profile(R.string.profile),
    EditReminder(R.string.editReminder),
    EditProfile(R.string.editProfile),
    Contact(R.string.contact)
}

data class NavItem(
    val screen: MediAlertScreen,
    val icon: Int,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediAlertApp(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    
    // Check if user is already logged in
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) MediAlertScreen.Home.name else MediAlertScreen.Login.name

    val currentScreen = try {
        MediAlertScreen.valueOf(currentRoute ?: startDestination)
    } catch (e: Exception) {
        MediAlertScreen.Home
    }

    val navItems = listOf(
        NavItem(MediAlertScreen.Appointment, R.drawable.baseline_calendar_month_24, "Appointment"),
        NavItem(MediAlertScreen.Medication, R.drawable.baseline_assignment_24, "Medication"),
        NavItem(MediAlertScreen.Home, R.drawable.baseline_home_24, "Home"),
        NavItem(MediAlertScreen.Reminder, R.drawable.baseline_alarm_24, "Reminder"),
        NavItem(MediAlertScreen.Profile, R.drawable.person_24dp_000000, "Profile")
    )

    // Only show bottom bar on main tabs
    val showBottomBar = currentRoute in listOf(
        MediAlertScreen.Home.name,
        MediAlertScreen.Appointment.name,
        MediAlertScreen.Medication.name,
        MediAlertScreen.Reminder.name,
        MediAlertScreen.Profile.name
    )

    // Show TopBar except on Home and Login/Register
    val showTopBar = currentRoute != MediAlertScreen.Home.name && 
                     currentRoute != MediAlertScreen.Login.name && 
                     currentRoute != MediAlertScreen.Register.name

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(currentScreen.title),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.screen.name,
                            onClick = {
                                navController.navigate(item.screen.name) {
                                    popUpTo(MediAlertScreen.Home.name) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(id = item.icon),
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label, fontSize = 10.sp) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = MediAlertScreen.Login.name) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(MediAlertScreen.Home.name) {
                            popUpTo(MediAlertScreen.Login.name) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(MediAlertScreen.Register.name)
                    }
                )
            }
            composable(route = MediAlertScreen.Register.name) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(MediAlertScreen.Home.name) {
                            popUpTo(MediAlertScreen.Login.name) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(MediAlertScreen.Login.name) {
                            popUpTo(MediAlertScreen.Register.name) { inclusive = true }
                        }
                    }
                )
            }
            composable(route = MediAlertScreen.Home.name) {
                HomeScreen(
                    onProfileClick = { navController.navigate(MediAlertScreen.Profile.name) },
                    onContactClick = { navController.navigate(MediAlertScreen.Contact.name) }
                )
            }
            composable(route = MediAlertScreen.Appointment.name) {
                AppointmentScreen()
            }
            composable(route = MediAlertScreen.Medication.name) {
                MedicationScreen()
            }
            composable(route = MediAlertScreen.Reminder.name) {
                ReminderScreen(
                    reminders = SampleData.medicationReminders,
                    onAddClick = { navController.navigate(MediAlertScreen.EditReminder.name) },
                    onEditClick = { navController.navigate(MediAlertScreen.EditReminder.name) }
                )
            }
            composable(route = MediAlertScreen.Profile.name) {
                ProfileScreen(
                    onEditClick = { navController.navigate(MediAlertScreen.EditProfile.name) },
                    onLogoutClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(MediAlertScreen.Login.name) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(route = MediAlertScreen.Contact.name) {
                ContactScreen()
            }
            composable(route = MediAlertScreen.EditProfile.name) {
                ProfileEditScreen(onSaveClick = { navController.popBackStack() })
            }
            composable(route = MediAlertScreen.EditReminder.name) {
                ReminderEditScreen(
                    existingReminder = null,
                    onSave = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}
