package com.nexvary.shurayh.core

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nexvary.shurayh.MainActivity
import com.nexvary.shurayh.R
import java.time.LocalDateTime
import java.time.ZoneId

object HearingReminderScheduler {
    const val CHANNEL_ID = "shurayh_hearings"
    private const val EXTRA_TITLE = "title"
    private const val EXTRA_BODY = "body"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تذكيرات الجلسات",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات SHURAYH للجلسات القانونية"
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    /**
     * Schedules an inexact reminder. The app intentionally avoids exact-alarm privileges.
     * Returns false when the requested time is not in the future.
     */
    fun schedule(
        context: Context,
        hearingId: String,
        caseTitle: String,
        court: String,
        reminderAt: LocalDateTime,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Boolean {
        val triggerAt = reminderAt.atZone(zoneId).toInstant().toEpochMilli()
        if (triggerAt <= System.currentTimeMillis()) return false
        ensureChannel(context)
        val intent = Intent(context, HearingReminderReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, "جلسة: $caseTitle")
            putExtra(EXTRA_BODY, court)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            hearingId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        return true
    }

    fun cancel(context: Context, hearingId: String) {
        val intent = Intent(context, HearingReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            hearingId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    internal fun readTitle(intent: Intent): String = intent.getStringExtra(EXTRA_TITLE).orEmpty()
    internal fun readBody(intent: Intent): String = intent.getStringExtra(EXTRA_BODY).orEmpty()
}

class HearingReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        HearingReminderScheduler.ensureChannel(context)
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, HearingReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(HearingReminderScheduler.readTitle(intent).ifBlank { "تذكير جلسة" })
            .setContentText(HearingReminderScheduler.readBody(intent))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (_: SecurityException) {
            // Android 13+ may deny POST_NOTIFICATIONS. The app must respect that user choice.
        }
    }
}
