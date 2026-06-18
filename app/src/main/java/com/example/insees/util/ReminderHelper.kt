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
}