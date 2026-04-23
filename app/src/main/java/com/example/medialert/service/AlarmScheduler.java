package com.example.medialert.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.medialert.data.Appointment;
import com.example.medialert.data.Medication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.List;

public class AlarmScheduler {
    private Context context;
    private FirebaseFirestore db;
    private String userId;

    public AlarmScheduler(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    public void scheduleAllAlarms() {
        if (userId == null) return;
        scheduleMedicationAlarms();
        scheduleAppointmentAlarms();
    }

    private void scheduleMedicationAlarms() {
        db.collection("patients").document(userId).collection("medications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Medication med = doc.toObject(Medication.class);
                        List<String> times = med.getTimes();
                        if (times != null) {
                            for (String time : times) {
                                scheduleMedicationAlarm(med.getName(), time);
                            }
                        }
                    }
                });
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleMedicationAlarm(String name, String timeStr) {
        // timeStr format: "09:00 AM"
        try {
            String[] parts = timeStr.split("[: ]");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            String amPm = parts[2];

            if (amPm.equalsIgnoreCase("PM") && hour < 12) hour += 12;
            if (amPm.equalsIgnoreCase("AM") && hour == 12) hour = 0;

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("type", "MEDICATION");
            intent.putExtra("title", "Peringatan Ubat");
            intent.putExtra("message", "Sila ambil ubat anda: " + name);

            int requestCode = (name + timeStr).hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } catch (Exception e) {
            Log.e("AlarmScheduler", "Error parsing time: " + timeStr, e);
        }
    }

    private void scheduleAppointmentAlarms() {
        db.collection("patients").document(userId).collection("appointments")
                .whereEqualTo("status", "Akan datang")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Appointment appt = doc.toObject(Appointment.class);
                        if (appt.getTimestamp() != null) {
                            scheduleAppointmentAlarm(appt.getDepartment(), appt.getTimestamp().toDate().getTime());
                        }
                    }
                });
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleAppointmentAlarm(String department, long apptTimeMillis) {
        // Schedule exactly 24 hours before
        long triggerTime = apptTimeMillis - (24 * 60 * 60 * 1000);

        if (triggerTime < System.currentTimeMillis()) return; // Already passed or too close

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("type", "APPOINTMENT");
        intent.putExtra("title", "Peringatan Temujanji");
        intent.putExtra("message", "Anda mempunyai temujanji di " + department + " esok.");

        int requestCode = (department + apptTimeMillis).hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }
}
