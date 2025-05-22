/**
 * @file PowerStateChangedReceiver.kt
 *
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import de.euhm.jlt.dao.TimesWork
import de.euhm.jlt.utils.Prefs
import de.euhm.jlt.utils.Constants

private val LOG_TAG: String = PowerStateChangedReceiver::class.java.simpleName

/**
 * The manifest Receiver is used to detect changes in battery state.
 * When the system broadcasts a "Battery Low" warning we turn off
 * the passive location updates to conserve battery when the app is
 * in the background.
 *
 * When the system broadcasts "Battery OK" to indicate the battery
 * has returned to an okay state, the passive location updates are
 * resumed.
 *
 * To test BATTERY_LOW try this:
 * > adb -s device-id shell am broadcast -a android.intent.action.BATTERY_LOW
 * > adb -s device-id shell am broadcast -a android.intent.action.BATTERY_OKAY
 * How to get device id? Get list of connected devices with id's:
 * > ...\adt\sdk\platform-tools\adb devices
 */
class PowerStateChangedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        var flag = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // needed starting Android 12 (S = 31)
            flag = flag or PendingIntent.FLAG_IMMUTABLE
        }
        val batteryLow = Intent.ACTION_BATTERY_LOW == intent.action
        val prefs = Prefs(context)

        if (!prefs.widgetUpdateOnLowBattery) {
            val alarmMgr = context
                .getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            val pendingIntent = PendingIntent.getBroadcast(context,
                0, alarmIntent, flag)

            if (batteryLow) {
                // cancel alarm under low battery conditions
                alarmMgr.cancel(pendingIntent)
                Log.v(LOG_TAG, "Widget update alarm canceled, because of low battery state.")
            } else if (TimesWork(context).workStarted) {
                // work is started and battery state is good again
                // update the widget with an alarm, but do not wake up device ...
                context.sendBroadcast(alarmIntent)
                alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(),
                    Constants.WIDGET_UPDATE_INTERVAL, pendingIntent)
                Log.v(LOG_TAG, "Widget update alarm enabled, because battery state is ok again.")
            }
        }
        /*
		 * PackageManager pm = context.getPackageManager(); ComponentName
		 * passiveLocationReceiver = new ComponentName(context,
		 * PassiveLocationChangedReceiver.class);
		 * 
		 * // Disable the passive location update receiver when the battery
		 * state is low. // Disabling the Receiver will prevent the app from
		 * initiating the background // downloads of nearby locations.
		 * pm.setComponentEnabledSetting(passiveLocationReceiver, batteryLow ?
		 * PackageManager.COMPONENT_ENABLED_STATE_DISABLED :
		 * PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
		 * PackageManager.DONT_KILL_APP);
		 */
    }
}