package com.example.medialert.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.medialert.MainActivity;
import com.example.medialert.R;

public class NotificationHelper {
    private static final String CHANNEL_ID = "MEDIALERT_STRICT_CHANNEL_v100";
    private Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) return;
            
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Peringatan MediAlert",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Peringatan ubat yang memerlukan pengesahan.");
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }
    }

    public void showNotification(String title, String message, String type) {
        // Apply strict behavior (un-swipable + "OK" button) ONLY to "MEDICATION".
        // Reminders and other types will be normal swipable notifications.
        boolean isStrictMedication = "MEDICATION".equalsIgnoreCase(type);

        int notificationId = (title + message + (type != null ? type : "")).hashCode();
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                context, 
                notificationId, 
                intent, 
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_alarm_24)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (isStrictMedication) {
            // Medication notifications are un-swipable
            builder.setOngoing(true);
            builder.setAutoCancel(false);
            
            // "OK" button is added ONLY for Medications
            Intent dismissIntent = new Intent(context, DismissReceiver.class);
            dismissIntent.putExtra("notificationId", notificationId);
            PendingIntent dismissPI = PendingIntent.getBroadcast(
                    context, 
                    notificationId, 
                    dismissIntent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.addAction(0, "OK", dismissPI);
        } else {
            // Other notifications (like REMINDER) are swipable and have no OK button
            builder.setAutoCancel(true);
            builder.setOngoing(false);
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            Notification n = builder.build();
            if (isStrictMedication) {
                // Ensure it can't be cleared by swiping or "Clear All"
                n.flags |= Notification.FLAG_ONGOING_EVENT;
                n.flags |= Notification.FLAG_NO_CLEAR;
            }
            manager.notify(notificationId, n);
            Log.d("NotificationHelper", "Posted notification: " + title + " (Strict: " + isStrictMedication + ")");
        }
    }
}
