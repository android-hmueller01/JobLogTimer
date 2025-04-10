/**
 * @file MainWidgetProvider.kt
 *
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import de.euhm.jlt.dao.TimesWork
import de.euhm.jlt.receivers.StartStopReceiver
import de.euhm.jlt.utils.Constants
import de.euhm.jlt.utils.TimeUtil

/**
 * JobLogTimer main widget provider
 * @author hmueller
 */
class MainWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        var intent: Intent
        var pendingIntent: PendingIntent?
        var flag = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // needed starting Android 12 (S = 31)
            flag = flag or PendingIntent.FLAG_IMMUTABLE
        }

        Log.v(LOG_TAG, "onUpdate()")
        val timesWork = TimesWork(context)
        for (currentWidgetId in appWidgetIds) {
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_main)

            // is work started?
            if (TimesWork.workStarted) {
                // update widget info line
                val curTimeMillis = TimeUtil.getCurrentTimeInMillis()
                val workTime = TimeUtil.getWorkedTime(context, TimesWork.timeStart, curTimeMillis, TimesWork.homeOffice, TimesWork.timeWorked)
                val overTime = TimeUtil.getOverTime(context, TimesWork.timeStart, curTimeMillis, TimesWork.homeOffice, TimesWork.timeWorked)
                remoteViews.setTextViewText(R.id.widget_info_line1, TimeUtil.formatTimeString24(workTime))
                remoteViews.setTextViewText(R.id.widget_info_line2, "(" + TimeUtil.formatTimeString24(overTime) + ")")
                // update the progress bar (worked time)
                val progress =
                    (100 * (curTimeMillis - TimesWork.timeStart).toFloat() / (timesWork.normalWorkEndTime - TimesWork.timeStart).toFloat()).toInt()
                //if (progress > 100) progress = 100;
                remoteViews.setProgressBar(R.id.widget_progress_bar, 100, progress, false)
                if (curTimeMillis > timesWork.normalWorkEndTime) {
                    if (curTimeMillis > timesWork.maxWorkEndTime) {
                        remoteViews.setViewVisibility(R.id.widget_progress_bar_red, View.VISIBLE)
                    } else {
                        remoteViews.setViewVisibility(R.id.widget_progress_bar_yellow, View.VISIBLE)
                        remoteViews.setViewVisibility(R.id.widget_progress_bar_red, View.INVISIBLE)
                    }
                } else {
                    remoteViews.setViewVisibility(R.id.widget_progress_bar_yellow, View.INVISIBLE)
                    remoteViews.setViewVisibility(R.id.widget_progress_bar_red, View.INVISIBLE)
                }
                // set new image for stop action
                remoteViews.setImageViewResource(R.id.widget_button_start_stop, R.drawable.ic_action_stop)
            } else {
                // update widget info line
                remoteViews.setTextViewText(R.id.widget_info_line1,
                    context.resources.getString(R.string.widget_info_line1))
                remoteViews.setTextViewText(R.id.widget_info_line2,
                    context.resources.getString(R.string.widget_info_line2))
                // update the progress bar (0%)
                remoteViews.setProgressBar(R.id.widget_progress_bar, 100, 0, false)
                remoteViews.setViewVisibility(R.id.widget_progress_bar_yellow, View.INVISIBLE)
                remoteViews.setViewVisibility(R.id.widget_progress_bar_red, View.INVISIBLE)
                // set new image for start action
                remoteViews.setImageViewResource(R.id.widget_button_start_stop, R.drawable.ic_action_start)

                val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flag)
                alarmMgr.cancel(pendingIntent)
            }
            // add onClickListener for Start/Stop
            intent = Intent(Constants.RECEIVER_START_STOP)
            // make the broadcast Intent explicit by specifying the receiver class
            intent.setClass(context, StartStopReceiver::class.java)
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flag)
            remoteViews.setOnClickPendingIntent(R.id.widget_button_start_stop, pendingIntent)
            // add onClickListener for Main App
            intent = Intent(context, MainActivity::class.java)
            intent.setAction(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            pendingIntent = PendingIntent.getActivity(context, 0, intent, flag or PendingIntent.FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.widget_button_StartApp, pendingIntent)
            // finally update the widget
            appWidgetManager.updateAppWidget(currentWidgetId, remoteViews)

            // BEWARE: Toast keeps device awake, as it counts as action and this is called cyclic!!!
            //Toast.makeText(context, "widget onUpdate id=" + currentWidgetId, Toast.LENGTH_SHORT).show();
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.v(LOG_TAG, "onReceive() intent '$action'")
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE == action) {
            val appWM = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWM.getAppWidgetIds(ComponentName(context, MainWidgetProvider::class.java))
            this.onUpdate(context, appWM, appWidgetIds)
        } else {
            super.onReceive(context, intent)
        }
    }

    override fun onDisabled(context: Context) {
        Log.v(LOG_TAG, "onDisabled()")

        super.onDisabled(context)
    }

    override fun onEnabled(context: Context) {
        Log.v(LOG_TAG, "onEnabled()")

        super.onEnabled(context)
    }

    companion object {
        private val LOG_TAG: String = MainWidgetProvider::class.java.simpleName
    }
}
