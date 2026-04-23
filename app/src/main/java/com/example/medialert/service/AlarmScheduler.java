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
import java.util.Locale;

public class AlarmScheduler {
    private final Context context;
    private final FirebaseFirestore db;

    public AlarmScheduler(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public void scheduleAllAlarms() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Log.w("AlarmScheduler", "UserId is null, skipping scheduling.");
            return;
        }
        Log.d("AlarmScheduler", "Starting to schedule all alarms for user: " + userId);
        scheduleMedicationAlarms(userId);
        scheduleAppointmentAlarms(userId);
    }

    private void scheduleMedicationAlarms(String userId) {
        db.collection("patients").document(userId).collection("medications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("AlarmScheduler", "Fetched " + queryDocumentSnapshots.size() + " medications");
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Medication med = doc.toObject(Medication.class);
                        
                        long now = System.currentTimeMillis();
                        if (med.getEndDate() != null && med.getEndDate().toDate().getTime() < now && !med.getUntilFinish()) {
                            continue;
                        }

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
        try {
            String cleanTime = timeStr.trim().toUpperCase(Locale.US);
            int hour, minute;
            String amPm;

            if (cleanTime.contains(" ")) {
                String[] parts = cleanTime.split("[: ]");
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
                amPm = parts[2];
            } else {
                hour = Integer.parseInt(cleanTime.substring(0, cleanTime.indexOf(":")));
                minute = Integer.parseInt(cleanTime.substring(cleanTime.indexOf(":") + 1, cleanTime.length() - 2));
                amPm = cleanTime.substring(cleanTime.length() - 2);
            }

            if (amPm.equals("PM") && hour < 12) hour += 12;
            if (amPm.equals("AM") && hour == 12) hour = 0;

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
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
            Log.e("AlarmScheduler", "Failed to parse medication time: " + timeStr, e);
        }
    }

    private void scheduleAppointmentAlarms(String userId) {
        db.collection("patients").document(userId).collection("appointments")
                .whereEqualTo("status", "Akan datang")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("AlarmScheduler", "Fetched " + queryDocumentSnapshots.size() + " appointments");
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Appointment appt = doc.toObject(Appointment.class);
                        if (appt.getTimestamp() != null) {
                            long apptTime = appt.getTimestamp().toDate().getTime();
                            
                            // 1. Reminder 24 hours before
                            scheduleAppointmentAlarm(appt.getDepartment(), apptTime - (24 * 60 * 60 * 1000), "Peringatan Esok");
                            
                            // 2. Reminder 12 hours before
                            scheduleAppointmentAlarm(appt.getDepartment(), apptTime - (12 * 60 * 60 * 1000), "Peringatan 12 Jam Lagi");
                            
                            // 3. Reminder at actual time
                            scheduleAppointmentAlarm(appt.getDepartment(), apptTime, "Janji temu anda sekarang");
                        }
                    }
                });
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleAppointmentAlarm(String department, long triggerTime, String label) {
        if (triggerTime < System.currentTimeMillis()) {
            return;
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("type", "APPOINTMENT");
        intent.putExtra("title", "MediAlert: " + label);
        intent.putExtra("message", "Anda mempunyai temujanji di " + department);

        // Unique request code for each trigger time
        int requestCode = (department + triggerTime).hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            Log.d("AlarmScheduler", "Appointment alarm set: " + label + " for " + department + " at " + new java.util.Date(triggerTime));
        }
    }
}
