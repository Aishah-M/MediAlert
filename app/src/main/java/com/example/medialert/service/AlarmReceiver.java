package com.example.medialert.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra("type");
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        if (title == null) title = "Peringatan MediAlert";
        if (message == null) message = "Sila ambil ubat anda.";

        Log.d("AlarmReceiver", "Received alarm: " + type + " - " + title);

        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.showNotification(title, message, type);

        // Reschedule alarms to set the next occurrence (e.g., for the next day)
        try {
            AlarmScheduler alarmScheduler = new AlarmScheduler(context);
            alarmScheduler.scheduleAllAlarms();
            
            ReminderScheduler reminderScheduler = new ReminderScheduler(context);
            reminderScheduler.rescheduleAll();
        } catch (Exception e) {
            Log.e("AlarmReceiver", "Reschedule failed: " + e.getMessage());
        }
    }
}
