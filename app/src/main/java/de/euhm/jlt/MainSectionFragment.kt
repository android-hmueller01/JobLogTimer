/**
 * @file MainSectionFragment.kt
 *
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import de.euhm.jlt.dao.Times
import de.euhm.jlt.dao.TimesDataSource
import de.euhm.jlt.dao.TimesWork
import de.euhm.jlt.preferences.Prefs
import de.euhm.jlt.utils.Constants
import de.euhm.jlt.utils.LongRef
import de.euhm.jlt.utils.TimeUtil
import java.util.Calendar
import java.util.Locale
import kotlin.math.floor

/**
 * Main section fragment of JobLog
 * @author hmueller
 */
class MainSectionFragment : Fragment() {
    @Suppress("PrivatePropertyName")
    private val LOG_TAG: String = MainSectionFragment::class.java.simpleName
    private lateinit var mContext: Context // gets initialized in onAttach()
    private lateinit var mTW: TimesWork // gets initialized in onCreate()
    private val mHandlerUpdateTimes = Handler(Looper.getMainLooper())
    private val mHandlerUpdateTimesDelay: Long = 1000

    /**
     * @return the mOnSwipeTouchListener
     */
    lateinit var onSwipeTouchListener: OnSwipeTouchListener

    /**
     * A [BroadcastReceiver] to update the view in this fragment.<br></br>
     * Register in onCreate() and unregister in onDestroy()!
     */
    private val receiverUpdateView: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.v(LOG_TAG, "Received BroadcastReceiver " + Constants.RECEIVER_UPDATE_VIEW)
            // update times view and statistics
            updateTimesView()
            updateStatisticsView()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        // get current Work Times DAO
        mTW = TimesWork(mContext)

        // register the update view receiver to update view from service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext.registerReceiver(receiverUpdateView,
                IntentFilter(Constants.RECEIVER_UPDATE_VIEW),
                Context.RECEIVER_NOT_EXPORTED)
        } else {
            mContext.registerReceiver(receiverUpdateView, IntentFilter(Constants.RECEIVER_UPDATE_VIEW))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_section_main, container, false)

        // setup the left/right main statistics swipe gesture
        onSwipeTouchListener = object : OnSwipeTouchListener(mContext, view.findViewById(R.id.layout_main_statistics)) {
            override fun onSwipeRight(): Boolean {
                //Toast.makeText(mContext, "Right swipe [Previous]", Toast.LENGTH_SHORT).show();
                val cal = TimesWork.statisticsDate
                cal.add(Calendar.WEEK_OF_YEAR, -1)
                updateStatisticsView()
                return true
            }

            override fun onSwipeLeft(): Boolean {
                //Toast.makeText(mContext, "Left swipe [Next]", Toast.LENGTH_SHORT).show();
                var result = false

                val cal = TimesWork.statisticsDate
                val now = Calendar.getInstance()
                // do not change cal before we checked if user wants to swipe after current
                // date, because TimesWork.getStatisticsDate() returns a static reference!
                now.add(Calendar.WEEK_OF_YEAR, -1)
                if (cal.before(now)) {
                    cal.add(Calendar.WEEK_OF_YEAR, 1)
                    updateStatisticsView()
                    result = true
                }
                return result
            }

            override fun onDoubleTap(): Boolean {
                //Toast.makeText(mContext, "Double tap [Current Date]", Toast.LENGTH_SHORT).show();
                TimesWork.statisticsDate = TimeUtil.getCurrentTime()
                updateStatisticsView()
                return true
            }
        }

        return view
    }

    @SuppressLint("SetTextI18n")
    fun updateTimesView() {
        val view = view
        // only update the view if all global variables are valid ...
        if (view == null) {
            Log.d(LOG_TAG, "view is null. Not executing updateTimesView() ...")
            return
        }

        var tv: TextView
        val v: View
        var params: RelativeLayout.LayoutParams
        val prefs = Prefs(mContext)
        var curTimeMillis = TimeUtil.getCurrentTimeInMillis()

        if (!TimesWork.workStarted && (TimesWork.timeStart != -1L) && (TimesWork.timeEnd != -1L)) {
            // work is done and times are set
            curTimeMillis = TimesWork.timeEnd
        }

        // update current time in seconds
        tv = view.findViewById(R.id.current_time)
        tv.text =
            String.format(Locale.getDefault(), resources.getString(R.string.current_time_text), Calendar.getInstance())

        // calculate the percentage worked
        val percentCurrent =
            (curTimeMillis - TimesWork.timeStart + TimesWork.timeWorked).toFloat() / (mTW.normalWorkEndTime - TimesWork.timeStart + TimesWork.timeWorked).toFloat()
        val percentNormal =
            (mTW.normalWorkEndTime - TimesWork.timeStart + TimesWork.timeWorked).toFloat() / (mTW.maxWorkEndTime - TimesWork.timeStart + TimesWork.timeWorked).toFloat()
        val percentProgressBar =
            (curTimeMillis - TimesWork.timeStart + TimesWork.timeWorked).toFloat() / (mTW.maxWorkEndTime - TimesWork.timeStart + TimesWork.timeWorked).toFloat()

        val tvDate = view.findViewById<TextView>(R.id.date_val)
        val tvTime = view.findViewById<TextView>(R.id.start_val)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        if (TimesWork.timeStart == -1L) {
            tvDate.text = " --.--.---- "
            tvTime.text = " --:-- "
            progressBar.secondaryProgress = 0
            progressBar.progress = 0
            tv = view.findViewById(R.id.progress_bar_percent_val)
            tv.text = "-"
            tv = view.findViewById(R.id.progress_bar_time_1)
            tv.text = "-"
            tv = view.findViewById(R.id.progress_bar_time_2)
            tv.text = "-"
            tv = view.findViewById(R.id.progress_bar_time_3)
            tv.text = "-"
        } else {
            // view start date and time
            tvDate.text = String.format(Locale.getDefault(), " %1\$td.%1\$tm.%1\$tY ", TimesWork.calStart)
            tvTime.text = String.format(Locale.getDefault(), " %tR ", TimesWork.calStart)

            // set home office check box
            val homeOfficeCb = view.findViewById<CheckBox>(R.id.homeoffice_cb)
            homeOfficeCb.isChecked = TimesWork.homeOffice

            // set the progress bar values
            val workedTimeString = TimeUtil.formatTimeString(TimesWork.timeWorked + TimeUtil.getWorkedTime(mContext,
                TimesWork.timeStart,
                curTimeMillis,
                TimesWork.homeOffice))
            val addProgressBarInfo: String
            val progress: Int
            val secondaryProgress: Int
            if (curTimeMillis > mTW.normalWorkEndTime) {
                // we are in over time ...
                progress = floor((percentNormal * 100).toDouble()).toInt()
                secondaryProgress = floor((percentProgressBar * 100).toDouble()).toInt()
                addProgressBarInfo = "(" + TimeUtil.formatTimeString(TimesWork.timeWorked + TimeUtil.getOverTime(
                    mContext,
                    TimesWork.timeStart,
                    curTimeMillis,
                    TimesWork.homeOffice)) + ")"
            } else {
                // we are in normal work time, no secondaryProgress needed
                progress = floor((percentProgressBar * 100).toDouble()).toInt()
                secondaryProgress = 0
                addProgressBarInfo = if (prefs.viewPercentEnabled) {
                    "(" + floor((percentCurrent * 100).toDouble()).toInt() + "%)"
                } else {
                    "(" + TimeUtil.formatTimeString(TimesWork.timeWorked + TimeUtil.getOverTime(mContext,
                        TimesWork.timeStart,
                        curTimeMillis,
                        TimesWork.homeOffice)) + ")"
                }
            }
            progressBar.secondaryProgress = secondaryProgress
            progressBar.progress = progress


            // set the progress bar text
            tv = view.findViewById(R.id.progress_bar_percent_val)
            tv.text = "$workedTimeString $addProgressBarInfo"


            // set tick 1 values
            tv = view.findViewById(R.id.progress_bar_time_1)
            tv.text = String.format(Locale.getDefault(), "%tR", TimesWork.calStart)


            // set tick 2 values
            v = view.findViewById(R.id.progress_bar_tick_2)
            val pos = floor((percentNormal * progressBar.width).toDouble()).toInt()
            //v.setX(progressBarLeft + pos - v.getWidth()); // API Level 11 needed -> use setLayoutParams()
            params = v.layoutParams as RelativeLayout.LayoutParams
            params.leftMargin = pos - v.width / 2
            v.layoutParams = params
            tv = view.findViewById(R.id.progress_bar_time_2)
            tv.text = String.format(Locale.getDefault(), "%tR", mTW.calNormalWorkEndTime)
            params = tv.layoutParams as RelativeLayout.LayoutParams
            params.leftMargin = pos - tv.width / 2
            tv.layoutParams = params


            // set tick 3 values
            tv = view.findViewById(R.id.progress_bar_time_3)
            tv.text = String.format(Locale.getDefault(), "%tR", mTW.calMaxWorkEndTime)
        }

        tv = view.findViewById(R.id.end_val)
        if (TimesWork.timeEnd == -1L) {
            tv.text = " --:-- "
        } else {
            tv.text = String.format(Locale.getDefault(), " %tR ", TimesWork.calEnd)
        }
    }

    private fun calcTimesInRange(overTimes: LongArray,
                                 calStart: Calendar,
                                 calEnd: Calendar,
                                 workedTime: LongRef,
                                 overTime: LongRef) {
        val prefs = Prefs(mContext)
        val db = TimesDataSource(mContext)
        val values = db.getTimeRangeTimes(calStart.timeInMillis, calEnd.timeInMillis, "ASC")
        db.close()
        val cnt = values.size
        workedTime.value = 0
        overTime.value = 0
        var workedPerDay: Long = 0 // worked time per day, to support multiple entries on the same day
        for (i in 0..6) overTimes[i] = 0
        for (i in 0..<cnt) {
            val ti = values[i]
            // do only calc worked time and not overtime, as we do not know jet, if we have more entries on the same day
            workedPerDay += TimeUtil.getWorkedTime(mContext, ti.timeStart, ti.timeEnd, ti.homeOffice)
            // do we have a next value?
            val tiNext = if (i + 1 < cnt) {
                // yes, use that
                values[i + 1]
            } else {
                // no, set day to next day (which is != current day) to finish calculating this day (see if below)
                Times(0, ti.timeStart + 24 * 60 * 60 * 1000, 0, ti.homeOffice)
            }
            // do we have more values with same day? 
            if (ti.calStart[Calendar.DAY_OF_MONTH] != tiNext.calStart[Calendar.DAY_OF_MONTH]) {
                // no, finish calculating this day
                workedTime.value += workedPerDay
                val ovt = workedPerDay - prefs.hoursInMillis
                overTime.value += ovt
                val dayOfWeek = ti.calStart[Calendar.DAY_OF_WEEK]
                overTimes[dayOfWeek - 1] += ovt
                workedPerDay = 0
            }
        }
    }

    private fun calcCalendarWeekRange(calStart: Calendar, calEnd: Calendar) {
        calStart[Calendar.HOUR_OF_DAY] = 0
        calStart[Calendar.MINUTE] = 0


        // calculate first day of week
        val dayOfWeek = calStart[Calendar.DAY_OF_WEEK]
        calStart.add(Calendar.DAY_OF_MONTH, 1 - dayOfWeek)


        // calculate last day of week
        calEnd.timeInMillis = calStart.timeInMillis
        calEnd.add(Calendar.DAY_OF_MONTH, 7)
    }

    private fun calcCalendarMonthRange(calStart: Calendar, calEnd: Calendar) {
        // calculate first day of month
        calStart[Calendar.HOUR_OF_DAY] = 0
        calStart[Calendar.MINUTE] = 0
        calStart[Calendar.DAY_OF_MONTH] = 1

        // calculate last day of month
        calEnd.timeInMillis = calStart.timeInMillis
        calEnd[Calendar.HOUR_OF_DAY] = 23
        calEnd[Calendar.MINUTE] = 59
        val maxDayOfMonth = calEnd.getActualMaximum(Calendar.DAY_OF_MONTH)
        calEnd[Calendar.DAY_OF_MONTH] = maxDayOfMonth
    }

    @SuppressLint("SetTextI18n")
    fun updateStatisticsView() {
        val view = view
        // only update the view if all global variables are valid ...
        if (view == null) {
            Log.d(LOG_TAG, "view is null. Not executing updateStatisticsView() ...")
            return
        }
        val calEnd = Calendar.getInstance()
        val workedTime = LongRef(0)
        val overTime = LongRef(0)
        val overTimes = LongArray(7)
        val weekTableIds = intArrayOf(0,
            R.id.week_table_mo_val,
            R.id.week_table_tu_val,
            R.id.week_table_we_val,
            R.id.week_table_th_val,
            R.id.week_table_fr_val,
            0)
        var tv: TextView


        // weekly statistics
        var calStart = TimesWork.statisticsDate.clone() as Calendar
        calcCalendarWeekRange(calStart, calEnd)
        calcTimesInRange(overTimes, calStart, calEnd, workedTime, overTime)
        Log.d(LOG_TAG,
            String.format(Locale.getDefault(),
                "weekly statistics: %1\$td.%1\$tm.%1\$tY %1\$tR - %2\$td.%2\$tm.%2\$tY %2\$tR",
                calStart,
                calEnd))

        calStart.add(Calendar.DAY_OF_MONTH, 1) // add one day, to get CW from Monday instead of Sunday
        tv = view.findViewById(R.id.stats_weekly_text)
        tv.text = String.format(Locale.getDefault(),
            resources.getString(R.string.stats_weekly_text),
            calStart[Calendar.WEEK_OF_YEAR])
        tv = view.findViewById(R.id.stats_weekly_worked_val)
        tv.text = TimeUtil.formatTimeString(workedTime.value)
        tv = view.findViewById(R.id.stats_weekly_overtime_val)
        tv.text = "(" + TimeUtil.formatTimeString(overTime.value) + ")"

        // fill week table
        for (i in 1..5) {
            if (weekTableIds[i] != 0) {
                tv = view.findViewById(weekTableIds[i])
                tv.text = TimeUtil.formatTimeString(overTimes[i])
                if (overTimes[i] < 0) {
                    tv.setTextColor(ContextCompat.getColor(mContext, R.color.sysRed))
                } else {
                    tv.setTextColor(ContextCompat.getColor(mContext, R.color.sysBlack))
                }
            }
        }

        // monthly statistics
        calStart = TimesWork.statisticsDate.clone() as Calendar
        calcCalendarMonthRange(calStart, calEnd)
        calcTimesInRange(overTimes, calStart, calEnd, workedTime, overTime)
        Log.d(LOG_TAG,
            String.format(Locale.getDefault(),
                "monthly statistics: %1\$td.%1\$tm.%1\$tY %1\$tR - %2\$td.%2\$tm.%2\$tY %2\$tR",
                calStart,
                calEnd))

        tv = view.findViewById(R.id.stats_monthly_text)
        tv.text = String.format(Locale.getDefault(),
            resources.getString(R.string.stats_monthly_text),
            DateFormat.format("MMM", calStart.timeInMillis))
        tv = view.findViewById(R.id.stats_monthly_worked_val)
        tv.text = TimeUtil.formatTimeString(workedTime.value)
        tv = view.findViewById(R.id.stats_monthly_overtime_val)
        tv.text = "(" + TimeUtil.formatTimeString(overTime.value) + ")"
    }

    override fun onResume() {
        super.onResume()
        updateTimesView()
        updateStatisticsView()
        mHandlerUpdateTimes.removeCallbacks(mUpdateTimesTask)
        // do another update in 100 millis, because at first call all pos. and widths are 0 ...
        mHandlerUpdateTimes.postDelayed(mUpdateTimesTask, 100)
    }

    override fun onPause() {
        super.onPause()
        // remove all pending posts
        mHandlerUpdateTimes.removeCallbacks(mUpdateTimesTask)
    }

    private val mUpdateTimesTask: Runnable = object : Runnable {
        override fun run() {
            updateTimesView()
            // set next update post delayed
            mHandlerUpdateTimes.postDelayed(this, mHandlerUpdateTimesDelay)
        }
    }

    init {
        // save this in static MainActivity variable for later use, because
        // class gets reinstated outside MainActivity e.g. by rotating the device
        MainActivity.mMainSectionFragment = this
    }

    override fun onDestroy() {
        super.onDestroy()
        mContext.unregisterReceiver(receiverUpdateView)
        //  clean up stored references to avoid leaking
    }
}
