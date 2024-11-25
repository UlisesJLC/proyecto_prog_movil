package com.example.inventory.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.inventory.work.sendNotification

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val channelId = intent.getStringExtra("channel_id") ?: "default_channel"
        val title = intent.getStringExtra("title") ?: "Reminder"
        val description = intent.getStringExtra("description") ?: "Don't forget your task!"

        sendNotification(context, channelId, title, description)
    }
}
