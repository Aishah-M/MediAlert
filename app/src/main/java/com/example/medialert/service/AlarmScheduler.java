package com.example.medialert.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
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
            Log.w(TAG, "No user logged in. Skipping alarms.");
            return;
        }
        Log.d(TAG, "=== Syncing Alarms for UID: " + userId + " ===");
        
        fetchAndScheduleMedications(userId);
        fetchAndScheduleAppointments(userId);
        fetchAndScheduleReminders(userId);
    }

    private void fetchAndScheduleMedications(String userId) {
        db.collection("patients").document(userId).collection("medications")
                .get()
                .addOnSuccessListener(snapshots -> {
                    Log.d(TAG, "[Medications] Found: " + snapshots.size());
                    for (DocumentSnapshot doc : snapshots) {
                        String name = doc.getString("name");
                        if (name == null) name = doc.getString("medicationName");
                        if (name == null) name = "Ubat Preskripsi";

                        // Scheduling validation
                        Boolean untilFinish = doc.getBoolean("untilFinish");
                        Timestamp endTs = doc.getTimestamp("endDate");
                        long now = System.currentTimeMillis();
                        if (endTs != null && endTs.toDate().getTime() < now && (untilFinish == null || !untilFinish)) {
                            continue;
                        }

                        List<String> times = (List<String>) doc.get("times");
                        if (times != null) {
                            for (String timeStr : times) {
                                scheduleAlarm(name, timeStr, doc.getId(), "MEDICATION");
                            }
                        }
                    }
                });
    }

    private void fetchAndScheduleReminders(String userId) {
        db.collection("patients").document(userId).collection("reminders")
                .get()
                .addOnSuccessListener(snapshots -> {
                    Log.d(TAG, "[Reminders] Found: " + snapshots.size());
                    for (DocumentSnapshot doc : snapshots) {
                        String name = doc.getString("medicationName");
                        if (name == null) name = "Peringatan";

                        // Scheduling validation (Stop if past endDate)
                        Boolean untilFinish = doc.getBoolean("untilFinish");
                        Timestamp endTs = doc.getTimestamp("endDate");
                        long now = System.currentTimeMillis();
                        if (endTs != null && endTs.toDate().getTime() < now && (untilFinish == null || !untilFinish)) {
                            Log.d(TAG, "Skipping expired reminder: " + name);
                            continue;
                        }

                        List<String> times = (List<String>) doc.get("times");
                        if (times != null) {
                            for (String timeStr : times) {
                                scheduleAlarm(name, timeStr, doc.getId(), "REMINDER");
                            }
                        }
                    }
                });
    }

    private void fetchAndScheduleAppointments(String userId) {
        db.collection("patients").document(userId).collection("appointments")
                .get()
                .addOnSuccessListener(snapshots -> {
                    Log.d(TAG, "[Appointments] Found: " + snapshots.size());
                    for (DocumentSnapshot doc : snapshots) {
                        String status = doc.getString("status");
                        if (status != null && status.trim().equalsIgnoreCase("Akan datang")) {
                            Timestamp ts = doc.getTimestamp("timestamp");
                            if (ts != null) {
                                Date apptDate = ts.toDate();
                                String dept = doc.getString("department");
                                if (dept == null) dept = "Klinik";
                                
                                long apptMillis = apptDate.getTime();
                                setApptAlarm(dept, apptMillis - (24 * 3600000L), "Esok", doc.getId() + "_24");
                                setApptAlarm(dept, apptMillis - (12 * 3600000L), "12 Jam Lagi", doc.getId() + "_12");
                                setApptAlarm(dept, apptMillis, "Sekarang", doc.getId() + "_now");
                            }
                        }
                    }
                });
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleAlarm(String name, String timeStr, String id, String type) {
        Calendar calendar = parseFlexibleTime(timeStr);
        if (calendar == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("type", type);
        intent.putExtra("title", type.equals("MEDICATION") ? "Peringatan Ubat (Hospital)" : "Peringatan Peribadi");
        intent.putExtra("message", "Masa untuk ambil: " + name);

        int requestCode = (id + timeStr + type).hashCode();
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
            Log.d(TAG, "ALARM SET: [" + type + "] " + name + " at " + calendar.getTime());
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setApptAlarm(String dept, long trigger, String label, String key) {
        if (trigger < System.currentTimeMillis()) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("type", "APPOINTMENT");
        intent.putExtra("title", "Janji Temu: " + label);
        intent.putExtra("message", "Anda ada janji temu di " + dept);

        int requestCode = key.hashCode();
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
            Log.d(TAG, "APPT SET: " + dept + " (" + label + ") at " + new Date(trigger));
        }
    }

    private Calendar parseFlexibleTime(String timeStr) {
        try {
            String clean = timeStr.trim().toUpperCase(Locale.US).replace(" ", "");
            int hour, minute = 0;
            String amPm = "";

            if (clean.endsWith("PM")) { amPm = "PM"; clean = clean.replace("PM", ""); }
            else if (clean.endsWith("AM")) { amPm = "AM"; clean = clean.replace("AM", ""); }

            if (clean.contains(":")) {
                String[] p = clean.split(":");
                hour = Integer.parseInt(p[0]);
                minute = Integer.parseInt(p[1]);
            } else {
                hour = Integer.parseInt(clean);
            }

            if (amPm.equals("PM") && hour < 12) hour += 12;
            if (amPm.equals("AM") && hour == 12) hour = 0;

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            if (cal.getTimeInMillis() <= System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR, 1);
            return cal;
        } catch (Exception e) {
            Log.e(TAG, "Bad time format: " + timeStr);
            return null;
        }
    }
}
