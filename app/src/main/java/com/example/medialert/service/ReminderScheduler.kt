package com.example.medialert.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.medialert.data.AppDatabase
import com.example.medialert.data.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun rescheduleAll() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val reminders = db.reminderDao().getAllReminders().first()
                reminders.forEach { scheduleAlarms(it) }
                Log.d("ReminderScheduler", "Rescheduled ${reminders.size} local reminders")
            } catch (e: Exception) {
                Log.e("ReminderScheduler", "Failed to reschedule all: ${e.message}")
            }
        }
    }

    fun scheduleAlarms(reminder: Reminder) {
        cancelAlarms(reminder)

        if (reminder.remainingStock <= 0) {
            Log.d("ReminderScheduler", "Skipping ${reminder.medicationName}: Out of stock")
            return
        }

        val now = System.currentTimeMillis()
        val start = reminder.startDate ?: 0L
        val end = reminder.endDate ?: Long.MAX_VALUE

        // If the entire period is in the past, skip
        if (!reminder.untilFinish && end < now) {
            Log.d("ReminderScheduler", "Skipping ${reminder.medicationName}: Period expired")
            return
        }

        reminder.times.forEach { timeStr ->
            val calendar = parseTime(timeStr) ?: return@forEach
            
            // If the calculated time for "today" has passed, move to tomorrow
            if (calendar.timeInMillis < now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            // Adjust to startDate if the next occurrence is before start
            if (calendar.timeInMillis < start) {
                val startCal = Calendar.getInstance().apply { timeInMillis = start }
                calendar.set(Calendar.YEAR, startCal.get(Calendar.YEAR))
                calendar.set(Calendar.MONTH, startCal.get(Calendar.MONTH))
                calendar.set(Calendar.DAY_OF_MONTH, startCal.get(Calendar.DAY_OF_MONTH))
                
                // If after moving to startDate, it's still in the past, move forward
                if (calendar.timeInMillis < now) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            // Final check against end date
            if (!reminder.untilFinish && calendar.timeInMillis > end) {
                return@forEach
            }

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("type", "PERSONAL_REMINDER")
                putExtra("title", "Masa Makan Ubat")
                putExtra("message", "Sila ambil ${reminder.dosage} ${reminder.unit} ${reminder.medicationName}")
                putExtra("reminderId", reminder.id)
            }

            val requestCode = (reminder.id + timeStr).hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
                Log.d("ReminderScheduler", "Alarm SET for ${reminder.medicationName} at ${calendar.time}")
            } catch (e: Exception) {
                Log.e("ReminderScheduler", "Error setting alarm: ${e.message}")
            }
        }
    }

    fun cancelAlarms(reminder: Reminder) {
        reminder.times.forEach { timeStr ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val requestCode = (reminder.id + timeStr).hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    private fun parseTime(timeStr: String): Calendar? {
        return try {
            val clean = timeStr.trim().uppercase(Locale.US).replace(" ", "")
            var hour: Int
            var minute = 0
            var amPm = ""

            if (clean.endsWith("PM")) {
                amPm = "PM"
                val t = clean.replace("PM", "")
                if (t.contains(":")) {
                    val parts = t.split(":")
                    hour = parts[0].toInt()
                    minute = parts[1].toInt()
                } else hour = t.toInt()
            } else if (clean.endsWith("AM")) {
                amPm = "AM"
                val t = clean.replace("AM", "")
                if (t.contains(":")) {
                    val parts = t.split(":")
                    hour = parts[0].toInt()
                    minute = parts[1].toInt()
                } else hour = t.toInt()
            } else {
                if (clean.contains(":")) {
                    val parts = clean.split(":")
                    hour = parts[0].toInt()
                    minute = parts[1].toInt()
                } else hour = clean.toInt()
            }

            if (amPm == "PM" && hour < 12) hour += 12
            if (amPm == "AM" && hour == 12) hour = 0

            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (e: Exception) {
            null
        }
    }
}
