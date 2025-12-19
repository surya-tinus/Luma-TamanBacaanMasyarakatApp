package com.example.luma.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast

object AlarmScheduler {

    // Hapus anotasi @SuppressLint("ScheduleExactAlarm") karena kita tidak pakai Exact lagi
    fun scheduleNotification(context: Context, timeInMillis: Long, title: String, message: String, reqCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("id", reqCode)
        }

        // FLAG_IMMUTABLE wajib untuk Android 12+
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // === BAGIAN YANG DIUBAH (SOLUSI CRASH) ===
        // Kita ganti dari 'setExactAndAllowWhileIdle' menjadi 'set' biasa.
        // 'set' memperbolehkan OS untuk sedikit menggeser waktu (beberapa detik/menit)
        // demi menghemat baterai. Ini tidak butuh izin SCHEDULE_EXACT_ALARM.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        }

        Toast.makeText(context, "Pengingat diatur!", Toast.LENGTH_SHORT).show()
    }
}