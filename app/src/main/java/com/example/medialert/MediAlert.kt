package com.example.medialert

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.medialert.screen.AppointmentScreen
import com.example.medialert.screen.ContactScreen
import com.example.medialert.screen.HomeScreen
import com.example.medialert.screen.MedicationScreen
import com.example.medialert.screen.ProfileEditScreen
import com.example.medialert.screen.ProfileScreen
import com.example.medialert.screen.ReminderEditScreen
import com.example.medialert.screen.ReminderScreen
import com.example.medialert.theme.MediAlertTheme
import com.example.medialert.data.SampleData

enum class MediAlertScreen(@StringRes val title: Int) {
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
    val currentScreen = MediAlertScreen.valueOf(
        currentRoute ?: MediAlertScreen.Home.name
    )
    val navItems = listOf(

        NavItem(MediAlertScreen.Appointment,R.drawable.baseline_calendar_month_24,"Appointment"),
        NavItem(MediAlertScreen.Medication,R.drawable.baseline_assignment_24,"Medication"),
        NavItem(MediAlertScreen.Home,R.drawable.baseline_home_24,"Home"),
        NavItem(MediAlertScreen.Reminder,R.drawable.baseline_alarm_24,"Reminder"),
        NavItem(MediAlertScreen.Profile,R.drawable.person_24dp_000000,"Profile")
    )

    val showBottomBar = currentRoute in listOf(
        MediAlertScreen.Home.name,
        MediAlertScreen.Appointment.name,
        MediAlertScreen.Medication.name,
        MediAlertScreen.Reminder.name,
        MediAlertScreen.EditReminder.name,
        MediAlertScreen.Profile.name,
        MediAlertScreen.EditProfile.name,
        MediAlertScreen.Contact.name
    )

    val showTopBar = currentRoute != MediAlertScreen.Home.name

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(currentScreen.title),
                                textAlign = TextAlign.Left,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                contentDescription = "Back"
                            )
                        }
                    },
                    // Optional: match your bottom bar color
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
                    modifier = Modifier.height(95.dp)
                ) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.screen.name,
                            onClick = {
                                navController.navigate(item.screen.name) {
                                    popUpTo(MediAlertScreen.Home.name) {
                                            saveState = false  // don't save state so CallScreen is never restored
                                            inclusive = false
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(id = item.icon),
                                    contentDescription=item.label
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = MediAlertScreen.Home.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = MediAlertScreen.Home.name) {
                HomeScreen(
                    onCallClick = {
                        navController.navigate(MediAlertScreen.Contact.name)
                    }
                )
            }
            composable(route = MediAlertScreen.Appointment.name) {
                AppointmentScreen(
                )
            }
            composable(route = MediAlertScreen.Medication.name) {
                MedicationScreen(
                )
            }
            composable(route = MediAlertScreen.Reminder.name) {
                ReminderScreen(
                    reminders = SampleData.medicationReminders,
                    onAddClick = {
                        navController.navigate(MediAlertScreen.EditReminder.name)
                    },
                    onEditClick = { reminder ->
                        // For now, we just navigate.
                        // To pass the actual data, you would normally use a ViewModel.
                        navController.navigate(MediAlertScreen.EditReminder.name)
                    }
                )
            }
            composable(route = MediAlertScreen.Profile.name) {
                ProfileScreen(
                )
            }
            composable(route = MediAlertScreen.Contact.name) {
                ContactScreen(
                )
            }
            composable(route = MediAlertScreen.EditProfile.name) {
                ProfileEditScreen(
                )
            }
            composable(route = MediAlertScreen.EditReminder.name) {
                ReminderEditScreen(
                    existingReminder = null, // null because we are adding new
                    onSave = { updatedReminder ->
                        // Logic to save would go here
                        navController.popBackStack()
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true) // ← showSystemUi = true shows full phone screen
@Composable
fun MediAlertAppPreview() {
    MediAlertTheme {
        MediAlertApp(
            navController = rememberNavController()
        )
    }
}

/*
private fun shareOrder(context: Context, subject: String, summary: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, summary)
    }
    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.new_cupcake_order)
        )
    )
}

private fun cancelOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
) {
    viewModel.resetOrder()
    navController.popBackStack(MediAlertScreen.Home.name, inclusive = false)
}
*/