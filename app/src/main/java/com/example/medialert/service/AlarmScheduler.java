package com.example.medialert.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
        if (userId == null) return;
        
        // Async fetch for UI-triggered updates
        db.collection("patients").document(userId).collection("medications").get()
                .addOnSuccessListener(this::processMedicationSnapshots);
        db.collection("patients").document(userId).collection("reminders").get()
                .addOnSuccessListener(this::processReminderSnapshots);
        db.collection("patients").document(userId).collection("appointments").get()
                .addOnSuccessListener(this::processAppointmentSnapshots);
    }

    /**
     * Synchronous version for use in Workers/Background threads
     */
    public void scheduleAllAlarmsSync() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        try {
            Log.d(TAG, "Starting Sync Alarm Scheduling...");
            
            QuerySnapshot medSnapshots = Tasks.await(db.collection("patients").document(userId).collection("medications").get(), 10, TimeUnit.SECONDS);
            processMedicationSnapshots(medSnapshots);

            QuerySnapshot remSnapshots = Tasks.await(db.collection("patients").document(userId).collection("reminders").get(), 10, TimeUnit.SECONDS);
            processReminderSnapshots(remSnapshots);

            QuerySnapshot apptSnapshots = Tasks.await(db.collection("patients").document(userId).collection("appointments").get(), 10, TimeUnit.SECONDS);
            processAppointmentSnapshots(apptSnapshots);
            
            Log.d(TAG, "Sync Alarm Scheduling Completed Successfully");
        } catch (Exception e) {
            Log.e(TAG, "Sync scheduling failed: " + e.getMessage());
        }
    }

    private void processMedicationSnapshots(QuerySnapshot snapshots) {
        if (snapshots == null) return;
        for (DocumentSnapshot doc : snapshots) {
            String name = doc.getString("name");
            if (name == null) name = doc.getString("medicationName");
            if (name == null) name = "Ubat";

            Boolean untilFinish = doc.getBoolean("untilFinish");
            Timestamp startTs = doc.getTimestamp("startDate");
            Timestamp endTs = doc.getTimestamp("endDate");
            
            List<String> times = (List<String>) doc.get("times");
            if (times != null) {
                for (String timeStr : times) {
                    scheduleAlarm(name, timeStr, doc.getId(), "MEDICATION", startTs, endTs, untilFinish != null && untilFinish);
                }
            }
        }
    }

    private void processReminderSnapshots(QuerySnapshot snapshots) {
        if (snapshots == null) return;
        for (DocumentSnapshot doc : snapshots) {
            String name = doc.getString("medicationName");
            if (name == null) name = "Peringatan";

            Boolean untilFinish = doc.getBoolean("untilFinish");
            Timestamp startTs = doc.getTimestamp("startDate");
            Timestamp endTs = doc.getTimestamp("endDate");

            List<String> times = (List<String>) doc.get("times");
            if (times != null) {
                for (String timeStr : times) {
                    scheduleAlarm(name, timeStr, doc.getId(), "REMINDER", startTs, endTs, untilFinish != null && untilFinish);
                }
            }
        }
    }

    private void processAppointmentSnapshots(QuerySnapshot snapshots) {
        if (snapshots == null) return;
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
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleAlarm(String name, String timeStr, String id, String type, Timestamp startTs, Timestamp endTs, boolean untilFinish) {
        Calendar calendar = parseFlexibleTime(timeStr);
        if (calendar == null) return;

        long now = System.currentTimeMillis();
        long startMillis = startTs != null ? startTs.toDate().getTime() : 0;
        long endMillis = endTs != null ? endTs.toDate().getTime() : Long.MAX_VALUE;

        // Date Range Validation
        if (!untilFinish && endMillis < now) return;

        if (calendar.getTimeInMillis() < startMillis) {
            Calendar startCal = Calendar.getInstance();
            startCal.setTimeInMillis(startMillis);
            calendar.set(Calendar.YEAR, startCal.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, startCal.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, startCal.get(Calendar.DAY_OF_MONTH));
            if (calendar.getTimeInMillis() <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        if (!untilFinish && calendar.getTimeInMillis() > endMillis) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("type", type);
        intent.putExtra("title", type.equals("MEDICATION") ? "Peringatan Ubat (Hospital)" : "Peringatan Peribadi");
        intent.putExtra("message", "Masa untuk ambil: " + name);

        int requestCode = (id + timeStr + type).hashCode();
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (am.canScheduleExactAlarms()) {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                    } else {
                        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                    }
                } else {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                }
                Log.d(TAG, "ALARM SET: [" + type + "] " + name + " at " + calendar.getTime());
            } catch (Exception e) {
                Log.e(TAG, "Failed to set alarm: " + e.getMessage());
            }
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
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
                } else {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to set appt alarm: " + e.getMessage());
            }
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
