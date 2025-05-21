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
import de.euhm.jlt.utils.AlarmUtils
import de.euhm.jlt.utils.Constants
import de.euhm.jlt.utils.Prefs
import de.euhm.jlt.utils.TimeUtil

private val LOG_TAG: String = StartStopReceiver::class.java.simpleName

class StartStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.v(LOG_TAG, "onReceive() intent '$action'")
        val prefs = Prefs(context)
        val timesWork = TimesWork(context)
        // Construct/load TimesWork DAO from persistent data
        if (timesWork.workStarted) {
            // End work ...
            if (timesWork.timeEnd == -1L) {
                // only use current time, if not set manually
                timesWork.timeEnd = TimeUtil.getCurrentTimeInMillis()
            }
            timesWork.workStarted = false

            // write data to database
            val dataSource = TimesDataSource(context)
            dataSource.createTimes()
            dataSource.close()

            // cancel alarms and notification
            AlarmUtils.setAlarms(context)

            // update views that changes take place
            context.sendBroadcast(Intent(Constants.RECEIVER_UPDATE_VIEW).setPackage(context.packageName))

            // update AppWidgetProvider
            context.sendBroadcast(Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).setPackage(context.packageName))

            Toast.makeText(context, R.string.work_ended, Toast.LENGTH_SHORT).show()
        } else {
            // Start work ...
            timesWork.workStarted = true
            timesWork.timeStart = TimeUtil.getCurrentTimeInMillis()
            timesWork.timeEnd = -1
            if (prefs.homeOfficeUseDefault) {
                // use default home office setting from prefs
                timesWork.homeOffice = prefs.homeOfficeDefaultSetting
            } // otherwise do not change old timesWork home office value

            val workedTimeDay = TimeUtil.getFinishedDayWorkTime(context, TimeUtil.getCurrentTime())
            timesWork.timeWorked = workedTimeDay

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
