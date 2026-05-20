package com.example.medialert.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.medialert.R
import com.example.medialert.data.AppDatabase
import java.text.SimpleDateFormat
import java.util.*

class MedicationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val reminderId = inputData.getString("reminderId") ?: return Result.failure()
        val db = AppDatabase.getDatabase(applicationContext)
        val reminder = db.reminderDao().getReminderById(reminderId) ?: return Result.success()

        val now = Calendar.getInstance()
        val currentTimeMillis = now.timeInMillis

        // 1. Inventory Check: Stop notifications if stock is 0
        if (reminder.remainingStock <= 0) {
            return Result.success()
        }

        // 2. Date Range Check
        val start = reminder.startDate ?: 0L
        val end = reminder.endDate ?: Long.MAX_VALUE
        
        // Use a small buffer for today start/end
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59); calendar.set(Calendar.MILLISECOND, 999)
        val endOfToday = calendar.timeInMillis

        // Don't notify if before start date
        if (currentTimeMillis < start && start > endOfToday) return Result.success()
        // Don't notify if after end date (unless untilFinish is true)
        if (!reminder.untilFinish && currentTimeMillis > end && end < startOfToday) return Result.success()

        // 3. Low Stock Notification: Trigger when exactly 3
        if (reminder.remainingStock == 3) {
            sendNotification(
                "Stok Ubat Rendah",
                "Ubat ${reminder.medicationName} hanya berbaki 3 ${reminder.unit}. Sila tambah stok segera.",
                (reminder.id + "_low_stock").hashCode()
            )
        }

        // 4. Time Check: Check if any scheduled time matches the current minute window
        val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currentTimeStr = timeFormatter.format(now.time)
        
        val isTimeForMed = reminder.times.any { it == currentTimeStr }
        
        if (isTimeForMed) {
            sendNotification(
                "Masa Makan Ubat",
                "Sila ambil ${reminder.dosage} ${reminder.unit} ${reminder.medicationName} sekarang.",
                reminder.id.hashCode()
            )
        }

        return Result.success()
    }

    private fun sendNotification(title: String, message: String, notificationId: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "medication_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Peringatan Ubat", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.baseline_call_24)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
