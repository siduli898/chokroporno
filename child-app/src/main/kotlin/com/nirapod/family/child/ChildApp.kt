package com.nirapod.family.child

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.nirapod.family.shared.util.Constants

class ChildApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "সুরক্ষা পরিষেবা"
            val descriptionText = "প্যারেন্টাল নিরাপত্তা ও সুরক্ষা মনিটরিং ব্যাকগ্রাউন্ডে চলছে"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(Constants.CHILD_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
