/**
 * @file StartStopReceiver.kt
 *
 * Global receiver to start/stop working (replaces old services, with did not work with Oreo in all cases)
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.receivers

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import de.euhm.jlt.R
import de.euhm.jlt.dao.TimesDataSource
import de.euhm.jlt.dao.TimesWork
import de.euhm.jlt.preferences.Prefs
import de.euhm.jlt.utils.AlarmUtils
import de.euhm.jlt.utils.Constants
import de.euhm.jlt.utils.TimeUtil

class StartStopReceiver : BroadcastReceiver() {
    @Suppress("PrivatePropertyName")
    private val LOG_TAG: String = StartStopReceiver::class.java.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.v(LOG_TAG, "onReceive() intent '$action'")
        val prefs = Prefs(context)

        // Construct/load TimesWork DAO from persistent data
        if (TimesWork.workStarted) {
            // End work ...
            if (TimesWork.timeEnd == -1L) {
                // only use current time, if not set manually
                TimesWork.timeEnd = TimeUtil.getCurrentTimeInMillis()
            }
            TimesWork.workStarted = false

            // write data to database
            val dataSource = TimesDataSource(context)
            dataSource.createTimes()
            dataSource.close()

            // Store TimesWork DAO to persistent data
            TimesWork.saveTimesWork(context)

            // cancel alarms and notification
            AlarmUtils.setAlarms(context)

            // update views that changes take place
            context.sendBroadcast(Intent(Constants.RECEIVER_UPDATE_VIEW).setPackage(context.packageName))

            // update AppWidgetProvider
            context.sendBroadcast(Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).setPackage(context.packageName))

            Toast.makeText(context, R.string.work_ended, Toast.LENGTH_SHORT).show()
        } else {
            // Start work ...
            TimesWork.workStarted = true
            TimesWork.timeStart = TimeUtil.getCurrentTimeInMillis()
            TimesWork.timeEnd = -1
            if (prefs.homeOfficeUseDefault) {
                // use default home office setting from prefs
                TimesWork.homeOffice = prefs.homeOfficeDefaultSetting
            } // otherwise do not change old timesWork home office value

            val workedTimeDay = TimeUtil.getFinishedDayWorkTime(context, TimeUtil.getCurrentTime())
            TimesWork.timeWorked = workedTimeDay

            // Store TimesWork DAO to persistent data
            TimesWork.saveTimesWork(context)

            // set alarms and notification
            AlarmUtils.setAlarms(context)

            // update views that changes take place
            context.sendBroadcast(Intent(Constants.RECEIVER_UPDATE_VIEW).setPackage(context.packageName))

            // update AppWidgetProvider
            context.sendBroadcast(Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).setPackage(context.packageName))

            Toast.makeText(context, R.string.work_started, Toast.LENGTH_SHORT).show()
        }
    }
}
