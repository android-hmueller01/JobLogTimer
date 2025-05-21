/**
 * @file AlarmReceiver.kt
 *
 * https://stackoverflow.com/questions/46304839/android-8-0-oreo-alarmmanager-with-broadcast-receiver-and-implicit-broadcast-ban
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import de.euhm.jlt.MainActivity
import de.euhm.jlt.R
import de.euhm.jlt.utils.Constants
import java.io.File
import androidx.core.net.toUri

private val LOG_TAG: String = AlarmReceiver::class.java.simpleName

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.v(LOG_TAG, "onReceive() intent '$action'")

        // Gets an instance of the NotificationManager service
        val notifyMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Set notification channel for Oreo and later
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(Constants.NOTIFICATION_CHANNEL,
                context.getString(R.string.notification_channel), NotificationManager.IMPORTANCE_HIGH)
            notifyMgr.createNotificationChannel(channel)
        }

        if (Constants.RECEIVER_NORMAL_WORK_ALARM == action) {
            // Builds the notification and issues it.
            notifyMgr.notify(Constants.NOTIFICATION_END_WORK, getNotification(context, true))
        } else if (Constants.RECEIVER_MAX_WORK_ALARM == action) {
            // Builds the notification and issues it.
            notifyMgr.notify(Constants.NOTIFICATION_END_WORK, getNotification(context, false))
        }
    }

    /**
     * Returns the [NotificationCompat] used as part of the foreground service.
     */
    private fun getNotification(context: Context, normalEnd: Boolean): Notification {
        var flag = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // needed starting Android 12 (S = 31)
            flag = flag or PendingIntent.FLAG_IMMUTABLE
        }
        val resultIntent = Intent(context, MainActivity::class.java)
        resultIntent.setAction(Intent.ACTION_MAIN)
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val resultPendingIntent = PendingIntent.getActivity(context, 0,
            resultIntent, flag or PendingIntent.FLAG_UPDATE_CURRENT)

        // make the broadcast Intent explicit by specifying the receiver class
        val endWorkIntent = Intent(Constants.RECEIVER_START_STOP, null, context,
            StartStopReceiver::class.java)
        val endWorkPendingIntent =
            PendingIntent.getBroadcast(context, 0, endWorkIntent, flag)

        val builder =
            NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_bw)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_color))
                .setContentTitle(context.resources.getText(R.string.app_name))
                .setSound(("android.resource://" + context.packageName + File.separator + R.raw.beeps).toUri()) // icon is not shown since Nougat https://stackoverflow.com/questions/41503972/icon-is-not-getting-displayed-in-notification-in-android-nougat
                .addAction(R.drawable.ic_action_stop, context.resources.getText(R.string.button_end),
                    endWorkPendingIntent)
                .setOngoing(false)
                .setContentIntent(resultPendingIntent)
        if (normalEnd) {
            builder.setContentText(context.resources.getText(R.string.work_time_end_notif_text))
                .setTicker(context.resources.getText(R.string.work_time_end_ticker))
                .setLights(ContextCompat.getColor(context, R.color.alarm_normal_work_time_led), 500, 1000)
        } else {
            builder.setContentText(context.resources.getText(R.string.work_time_max_notif_text))
                .setTicker(context.resources.getText(R.string.work_time_max_ticker))
                .setLights(ContextCompat.getColor(context, R.color.alarm_max_work_time_led), 200, 200)
        }

        return builder.build()
    }
}