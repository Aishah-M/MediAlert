package com.example.medialert.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.medialert.data.AppDatabase
import com.example.medialert.data.Reminder
import com.example.medialert.service.ReminderScheduler
import com.example.medialert.worker.MedicationWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ReminderVM(application: Application) : AndroidViewModel(application) {
    private val reminderDao = AppDatabase.getDatabase(application).reminderDao()
    private val workManager = WorkManager.getInstance(application)
    private val reminderScheduler = ReminderScheduler(application)

    val reminders: StateFlow<List<Reminder>> = reminderDao.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveReminder(reminder: Reminder, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            reminderDao.insertReminder(reminder)
            // Use AlarmManager for exact timing
            reminderScheduler.scheduleAlarms(reminder)
            // Keep WorkManager for periodic background checks (like inventory)
            scheduleMedicationWork(reminder)
            onSuccess()
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderDao.deleteReminder(reminder)
            reminderScheduler.cancelAlarms(reminder)
            cancelMedicationWork(reminder.id)
        }
    }

    private fun scheduleMedicationWork(reminder: Reminder) {
        cancelMedicationWork(reminder.id)
        
        if (reminder.remainingStock <= 0) return

        val data = Data.Builder()
            .putString("reminderId", reminder.id)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<MedicationWorker>(15, TimeUnit.MINUTES)
            .setInputData(data)
            .addTag(reminder.id)
            .build()

        workManager.enqueueUniquePeriodicWork(
            reminder.id,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun cancelMedicationWork(reminderId: String) {
        workManager.cancelUniqueWork(reminderId)
    }
}
