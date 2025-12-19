package com.example.luma.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.luma.R
import com.example.luma.MainActivity // Ganti dengan Activity utama kamu

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Pengingat Luma"
        val message = intent.getStringExtra("message") ?: "Jangan lupa kembalikan buku ya!"
        val notificationId = intent.getIntExtra("id", 0)

        showNotification(context, title, message, notificationId)
    }

    private fun showNotification(context: Context, title: String, message: String, notifId: Int) {
        val channelId = "luma_reminder_channel"
        val channelName = "Pengingat Pengembalian Buku"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Buat Notification Channel (Wajib untuk Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi untuk mengingatkan pengembalian buku"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Intent agar kalau notifikasi diklik, aplikasi terbuka
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Desain Notifikasinya
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_user) // Ganti dengan icon aplikasi kamu (ic_logo atau ic_book)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // 4. Tampilkan!
        notificationManager.notify(notifId, builder.build())
    }
}