package com.example.medialert

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.medialert.screen.*
import com.example.medialert.data.Reminder
import com.example.medialert.viewModel.ReminderVM
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel

enum class MediAlertScreen(@get:StringRes val title: Int) {
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

@Composable
fun MediAlertApp() {
    val isPreview = LocalInspectionMode.current
    val currentUser = if (isPreview) null else try {
        FirebaseAuth.getInstance().currentUser
    } catch (e: Exception) {
        null
    }
    
    val startDestination = if (currentUser != null) MediAlertScreen.Home.name else MediAlertScreen.Login.name
    
    MediAlertAppContent(startDestination = startDestination)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediAlertAppContent(
    navController: NavHostController = rememberNavController(),
    startDestination: String
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val isPreview = LocalInspectionMode.current
    val currentRoute = backStackEntry?.destination?.route ?: startDestination
    
    // State to hold the reminder being edited
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }

    val currentScreen = try {
        MediAlertScreen.valueOf(currentRoute)
    } catch (e: Exception) {
        MediAlertScreen.Home
    }

    val navItems = listOf(
        NavItem(MediAlertScreen.Appointment, R.drawable.baseline_calendar_month_24, "Janji Temu"),
        NavItem(MediAlertScreen.Medication, R.drawable.baseline_assignment_24, "Perubatan"),
        NavItem(MediAlertScreen.Home, R.drawable.baseline_home_24, "Utama"),
        NavItem(MediAlertScreen.Reminder, R.drawable.baseline_alarm_24, "Peringatan"),
        NavItem(MediAlertScreen.Profile, R.drawable.person_24dp_000000, "Profil")
    )

    val showBottomBar = currentRoute in listOf(
        MediAlertScreen.Home.name,
        MediAlertScreen.Appointment.name,
        MediAlertScreen.Medication.name,
        MediAlertScreen.Reminder.name,
        MediAlertScreen.Profile.name
    )

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
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.height(110.dp)
                ) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.screen.name,
                            onClick = {
                                navController.navigate(item.screen.name) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
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
                            label = { Text(text = item.label, fontSize = 10.sp) },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isPreview) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Pratonton Skrin: ${stringResource(currentScreen.title)}", style = MaterialTheme.typography.titleLarge)
            }
        } else {
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
                        onNavigateToRegister = { navController.navigate(MediAlertScreen.Register.name) }
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
                composable(route = MediAlertScreen.Appointment.name) { AppointmentScreen() }
                composable(route = MediAlertScreen.Medication.name) { MedicationScreen() }
                
                composable(route = MediAlertScreen.Reminder.name) {
                    val reminderVM: ReminderVM = viewModel()
                    val reminders by reminderVM.reminders.collectAsState()
                    
                    ReminderScreen(
                        reminders = reminders,
                        onAddClick = { 
                            editingReminder = null
                            navController.navigate(MediAlertScreen.EditReminder.name) 
                        },
                        onEditClick = { reminder ->
                            editingReminder = reminder
                            navController.navigate(MediAlertScreen.EditReminder.name)
                        },
                        onDeleteClick = { reminder -> reminderVM.deleteReminder(reminder) }
                    )
                }
                
                composable(route = MediAlertScreen.EditReminder.name) {
                    val reminderVM: ReminderVM = viewModel()
                    ReminderEditScreen(
                        existingReminder = editingReminder,
                        onSave = { reminder ->
                            reminderVM.saveReminder(reminder) {
                                navController.popBackStack()
                            }
                        },
                        onCancel = { navController.popBackStack() }
                    )
                }
                
                composable(route = MediAlertScreen.Profile.name) {
                    ProfileScreen(
                        onEditClick = { navController.navigate(MediAlertScreen.EditProfile.name) },
                        onLogoutClick = {
                            try { FirebaseAuth.getInstance().signOut() } catch (e: Exception) {}
                            navController.navigate(MediAlertScreen.Login.name) { popUpTo(0) { inclusive = true } }
                        }
                    )
                }
                composable(route = MediAlertScreen.Contact.name) { ContactScreen() }
                composable(route = MediAlertScreen.EditProfile.name) {
                    ProfileEditScreen(onSaveClick = { navController.popBackStack() })
                }
            }
        }
    }
}
