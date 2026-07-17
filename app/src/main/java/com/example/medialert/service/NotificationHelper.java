package com.example.medialert.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.medialert.MainActivity;
import com.example.medialert.R;

public class NotificationHelper {
    // Using a new version (v500) to ensure Android creates a fresh channel with sound enabled
    private static final String CHANNEL_ID = "MEDIALERT_NOTIF_CHANNEL_v500";
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
            channel.setDescription("Peringatan penting untuk ubat dan janji temu.");
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            
            // Explicitly set sound for the channel
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes);
            
            manager.createNotificationChannel(channel);
        }
    }

    public void showNotification(String title, String message, String type) {
        Log.d("NotificationHelper", "Showing notification: [" + type + "] " + title);
        
        boolean isMedication = "MEDICATION".equalsIgnoreCase(type);
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
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Force default sound/vibrate
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // For Medication, make it hard to miss with heads-up display
        if (isMedication) {
            builder.setFullScreenIntent(pi, true);
            builder.setOngoing(true);
            builder.setAutoCancel(false);
            
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
            builder.setAutoCancel(true);
            builder.setOngoing(false);
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            Notification n = builder.build();
            if (isMedication) {
                // Low-level flags to prevent swiping/clearing
                n.flags |= Notification.FLAG_ONGOING_EVENT;
                n.flags |= Notification.FLAG_NO_CLEAR;
            }
            manager.notify(notificationId, n);
        }
    }
}
