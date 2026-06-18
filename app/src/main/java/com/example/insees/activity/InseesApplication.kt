//package com.example.insees
//
//import android.app.Application
//import com.google.firebase.database.FirebaseDatabase
//
//class FirebaseOffline:Application() {
//    override fun onCreate() {
//        super.onCreate()
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
//    }
//}

package com.example.insees

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.google.firebase.database.FirebaseDatabase

class InseesApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        createNotificationChannel()
    }

    //Android 8.0 (API 26) aur uske baad har notification ek
    // Notification Channel ke through hi post hoti hai.
    // Channel notification ki category, importance,
    // sound aur vibration settings define karta hai.
    // Agar required channel create na kiya ho, to Android 8+
    // devices par notification display nahi hoti.

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val soundUri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                "task_reminder_channel",
                "Task Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Task reminder notifications"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 300, 300)
                setSound(soundUri, audioAttributes)
            }

            val notificationManager =
                getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(channel)
        }
    }
}