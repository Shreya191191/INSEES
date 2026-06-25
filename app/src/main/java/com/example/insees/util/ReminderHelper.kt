package com.example.insees.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Locale

object ReminderHelper {

    fun scheduleReminder(
        context: Context,
        title: String,
        description: String,
        date: String,
        time: String
    ) {
        Log.d("TEST", "Inside scheduleReminder")
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val reminderTime = sdf.parse("$date $time") ?: return

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("taskTitle", title)
            putExtra("taskDesc", description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            if (alarmManager.canScheduleExactAlarms()) {
                Log.d("REMINDER", "Scheduling for: ${reminderTime.time}")
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime.time,
                    pendingIntent
                )
            }

        } else {

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime.time,
                pendingIntent
            )
        }
    }

    fun scheduleDailyAttendanceReminder(context: Context) {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(java.util.Calendar.HOUR_OF_DAY, 17) // 5:00 PM
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }

        // If time is in the past, schedule for tomorrow
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("taskTitle", "Attendance Reminder")
            putExtra("taskDesc", "Have you updated your attendance today? Maintain your 75% target!")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            999, // Unique request code for daily attendance
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}