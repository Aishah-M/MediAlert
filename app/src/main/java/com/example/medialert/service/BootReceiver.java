package com.example.medialert.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
            "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {
            
            Log.d("BootReceiver", "Phone rebooted, rescheduling alarms...");
            
            // Reschedule Firestore alarms
            AlarmScheduler alarmScheduler = new AlarmScheduler(context);
            alarmScheduler.scheduleAllAlarms();

            // Reschedule local reminders
            ReminderScheduler reminderScheduler = new ReminderScheduler(context);
            reminderScheduler.rescheduleAll();
        }
    }
}
