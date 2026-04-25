package com.example.medialert.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.medialert.data.Appointment;
import com.example.medialert.data.Medication;
import com.example.medialert.data.Reminder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AlarmScheduler {
    private final Context context;
    private final FirebaseFirestore db;
    private static final String TAG = "AlarmScheduler";

    public AlarmScheduler(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public void scheduleAllAlarms() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Log.w(TAG, "UserId is null, skipping scheduling.");
            return;
        }
        Log.d(TAG, "Starting sync for user: " + userId);
        scheduleMedicationAlarms(userId);
        scheduleAppointmentAlarms(userId);
        scheduleCustomReminderAlarms(userId);
    }

    private void scheduleMedicationAlarms(String userId) {
        db.collection("patients").document(userId).collection("medications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Medications found: " + queryDocumentSnapshots.size());
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Medication med = doc.toObject(Medication.class);
                        
                        long now = System.currentTimeMillis();
                        if (med.getEndDate() != null && med.getEndDate().toDate().getTime() < now && !med.getUntilFinish()) {
                            Log.d(TAG, "Skipping expired med: " + med.getName());
                            continue;
                        }

                        List<String> times = med.getTimes();
                        if (times != null) {
                            for (String time : times) {
                                scheduleMedicationAlarm(med.getName(), time, doc.getId());
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching medications", e));
    }

    private void scheduleCustomReminderAlarms(String userId) {
        db.collection("patients").document(userId).collection("reminders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Custom reminders found: " + queryDocumentSnapshots.size());
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Reminder reminder = doc.toObject(Reminder.class);
                        List<String> times = reminder.getTimes();
                        if (times != null) {
                            for (String time : times) {
                                scheduleReminderAlarm(reminder.getMedicationName(), time, doc.getId());
                            }
                        }
                    }
                });
    }

    //MEDICATION
    @SuppressLint("ScheduleExactAlarm")
    private void scheduleMedicationAlarm(String name, String timeStr, String medId) {
        try {
            Calendar calendar = parseTime(timeStr);
            if (calendar == null) {
                Log.e(TAG, "Failed to parse medication time: " + timeStr);
                return;
            }

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("type", "MEDICATION");
            intent.putExtra("title", "Peringatan Ubat (Hospital)");
            intent.putExtra("message", "Sila ambil ubat preskripsi: " + name);

            int requestCode = (medId + timeStr + "MED").hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Log.d(TAG, "Scheduled Medication: " + name + " at " + calendar.getTime());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting medication alarm", e);
        }
    }

    //REMINDER
    @SuppressLint("ScheduleExactAlarm")
    private void scheduleReminderAlarm(String name, String timeStr, String reminderId) {
        try {
            Calendar calendar = parseTime(timeStr);
            if (calendar == null) return;

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("type", "REMINDER");
            intent.putExtra("title", "Peringatan Peribadi");
            intent.putExtra("message", "Masa untuk ambil: " + name);

            int requestCode = (reminderId + timeStr + "REM").hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Log.d(TAG, "Scheduled Reminder: " + name + " at " + calendar.getTime());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting reminder alarm", e);
        }
    }

    private Calendar parseTime(String timeStr) {
        try {
            String cleanTime = timeStr.trim().toUpperCase(Locale.US);
            int hour, minute = 0;
            String amPm;

            if (cleanTime.contains(":")) {
                String[] parts = cleanTime.split("[: ]");
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
                amPm = parts[parts.length - 1];
            } else {
                // Support "9 AM" format
                String[] parts = cleanTime.split(" ");
                hour = Integer.parseInt(parts[0]);
                amPm = parts[1];
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
            return calendar;
        } catch (Exception e) {
            Log.e(TAG, "Parse error for: " + timeStr);
            return null;
        }
    }

    private void scheduleAppointmentAlarms(String userId) {
        db.collection("patients").document(userId).collection("appointments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Appointments found: " + queryDocumentSnapshots.size());
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Appointment appt = doc.toObject(Appointment.class);
                        // Case-insensitive status check
                        if (appt.getStatus() != null && appt.getStatus().equalsIgnoreCase("Akan datang")) {
                            if (appt.getTimestamp() != null) {
                                long apptTime = appt.getTimestamp().toDate().getTime();
                                scheduleAppointmentAlarm(appt.getDepartment(), apptTime - (24 * 60 * 60 * 1000), "Esok", doc.getId() + "24");
                                scheduleAppointmentAlarm(appt.getDepartment(), apptTime - (12 * 60 * 60 * 1000), "12 Jam Lagi", doc.getId() + "12");
                                scheduleAppointmentAlarm(appt.getDepartment(), apptTime, "Sekarang", doc.getId() + "now");
                            }
                        }
                    }
                });
    }

    //APPOINTMENT
    @SuppressLint("ScheduleExactAlarm")
    private void scheduleAppointmentAlarm(String department, long triggerTime, String label, String id) {
        if (triggerTime < System.currentTimeMillis()) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("type", "APPOINTMENT");
        intent.putExtra("title", "Peringatan Janji Temu");
        intent.putExtra("message", label + ": Temujanji di " + department);

        int requestCode = id.hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            Log.d(TAG, "Scheduled Appointment (" + label + ") at " + new java.util.Date(triggerTime));
        }
    }
}
