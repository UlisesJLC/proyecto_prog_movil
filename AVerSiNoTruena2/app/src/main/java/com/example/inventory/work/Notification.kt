package com.example.inventory.work

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.inventory.R

fun sendNotification(context: Context, channelId: String, title: String, description: String) {
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_task_notification) // Ajusta el ícono
        .setContentTitle(title)
        .setContentText(description)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationId = System.currentTimeMillis().toInt()
    notificationManager.notify(notificationId, notification)

}
