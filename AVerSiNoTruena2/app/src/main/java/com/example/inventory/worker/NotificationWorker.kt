package com.example.inventory.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.inventory.R

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("task_title") ?: "Task Reminder"
        val description = inputData.getString("task_description") ?: "You have a task due!"

        Log.d("NotificationWorker", "Creating notification with title: $title and description: $description")

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal de notificación si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "task_channel",
                "Task Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationWorker", "Notification channel created: task_channel")
        }

        // Crear la notificación
        val notification = NotificationCompat.Builder(applicationContext, "task_channel")
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Cambia al ícono deseado
            .build()

        notificationManager.notify(1, notification)
        Log.d("NotificationWorker", "Notification sent with ID: 1")

        return Result.success()
    }
}
