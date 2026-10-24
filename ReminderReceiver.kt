package com.clientsaathi.hdfclife

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra("clientId", 0L)
        val client = ClientDb(context).use { it.get(id) } ?: return
        if (client.done) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "client_followups"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(
                channelId, "Client follow-up reminders", NotificationManager.IMPORTANCE_HIGH))
        }
        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            android.app.Notification.Builder(context, channelId)
        else android.app.Notification.Builder(context)
        manager.notify(id.toInt(), builder.setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Follow-up due: ${client.name}")
            .setContentText(client.reason.ifBlank { "Client follow-up reminder" })
            .setAutoCancel(true).setContentIntent(open).build())
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.rescheduleAll(context)
    }
}
