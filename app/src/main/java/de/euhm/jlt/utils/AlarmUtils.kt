/**
 * @file AlarmUtils.kt
 *
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import de.euhm.jlt.dao.TimesWork
import de.euhm.jlt.receivers.AlarmReceiver

/**
 * Helper class to set JobLogTimer alarms
 *
 * @author hmueller
 */
object AlarmUtils {
    private val LOG_TAG: String = AlarmUtils::class.java.simpleName

    /**
     * Set JobLogTimer alarms for normal and maximal work time, set update widget alarm.
     *
     * @param context   Context of application environment.
     */
    @JvmStatic
    fun setAlarms(context: Context) {
        // see http://android-er.blogspot.de/2010/10/simple-example-of-alarm-service-using.html
        // or http://android-er.blogspot.de/2011/05/using-alarmmanager-to-start-scheduled.html
        var alarmIntent: Intent
        var pendingIntent: PendingIntent?
        //Context context = getApplicationContext();
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val prefs = Prefs(context)
        val timesWork = TimesWork(context)
        var flag = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // needed starting Android 12 (S = 31)
            flag = flag or PendingIntent.FLAG_IMMUTABLE

            if (!alarmMgr.canScheduleExactAlarms()) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                // For Android 12 (API level 31) and above, you need to request this permission using an intent to open the system settings
                Log.v(LOG_TAG, "setAlarms(): Permission SCHEDULE_EXACT_ALARM not granted. Requesting permission ...")
                val uri = Uri.fromParts("package", context.packageName, null)
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).setData(uri)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).apply {
                    context.startActivity(this)
                }
                return
            }
        }

        if (TimesWork.workStarted) {
            // set alarm
            if (prefs.endHoursWarnEnabled) {
                // set an alarm to normal work hour, use explicit intent by specifying the receiver class
                alarmIntent = Intent(Constants.RECEIVER_NORMAL_WORK_ALARM)
                alarmIntent.setClass(context, AlarmReceiver::class.java)
                pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, flag)

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    Log.d(LOG_TAG, "alarmMgr.set(RECEIVER_NORMAL_WORK_ALARM)")
                    alarmMgr[AlarmManager.RTC_WAKEUP, timesWork.normalWorkEndTime] = pendingIntent
                } else {
                    Log.d(LOG_TAG, "alarmMgr.setExactAndAllowWhileIdle(RECEIVER_NORMAL_WORK_ALARM)")
                    alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        timesWork.normalWorkEndTime,
                        pendingIntent)
                }
            }

            if (prefs.maxHoursWarnEnabled) {
                // set a second alarm to max work hour, use explicit intent by specifying the receiver class
                alarmIntent = Intent(Constants.RECEIVER_MAX_WORK_ALARM)
                alarmIntent.setClass(context, AlarmReceiver::class.java)
                pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, flag)
                val alarmTime = timesWork.maxWorkEndTime - prefs.maxHoursWarnBeforeInMillis
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    Log.d(LOG_TAG, "alarmMgr.set(RECEIVER_MAX_WORK_ALARM)")
                    alarmMgr[AlarmManager.RTC_WAKEUP, alarmTime] = pendingIntent
                } else {
                    Log.d(LOG_TAG, "alarmMgr.setExactAndAllowWhileIdle(RECEIVER_MAX_WORK_ALARM)")
                    alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
                }
            }

            // update the widget with an alarm, but do not wake up device ...
            alarmIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, flag)
            // set repeating alarm only of we are not in power save mode
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isPowerSaveMode || prefs.widgetUpdateOnLowBattery) {
                alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(),
                    Constants.WIDGET_UPDATE_INTERVAL,
                    pendingIntent)
            }
        } else {
            // cancel alarm
            alarmIntent = Intent(Constants.RECEIVER_NORMAL_WORK_ALARM)
            alarmIntent.setClass(context, AlarmReceiver::class.java)
            pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, flag)
            alarmMgr.cancel(pendingIntent)

            alarmIntent = Intent(Constants.RECEIVER_MAX_WORK_ALARM)
            alarmIntent.setClass(context, AlarmReceiver::class.java)
            pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, flag)
            alarmMgr.cancel(pendingIntent)

            alarmIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, flag)
            alarmMgr.cancel(pendingIntent)

            // cancel/remove notification
            val notifyMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifyMgr.cancel(Constants.NOTIFICATION_END_WORK)
        }
    }
}
