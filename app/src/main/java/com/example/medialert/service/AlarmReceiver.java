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

        Log.d("AlarmReceiver", "Received alarm: " + type + " - " + title);

        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.showNotification(title, message, type);
    }
}
