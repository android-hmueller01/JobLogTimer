/**
 * @file ViewTimesListAdapter.kt
 * @author Holger Mueller
 *
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.listadapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.euhm.jlt.R
import de.euhm.jlt.dao.Times
import de.euhm.jlt.utils.TimeUtil.formatTimeString24
import de.euhm.jlt.utils.TimeUtil.getOverTime
import de.euhm.jlt.utils.TimeUtil.getWorkedTime
import java.util.Calendar
import java.util.Locale

/**
 * List adapter for Times
 *
 * @author hmueller
 */
class ViewTimesListAdapter(context: Context, layoutId: Int, listTimes: List<Times>) :
    ArrayAdapter<Times>(context, layoutId, listTimes) {
    @Suppress("PrivatePropertyName")
    private val LOG_TAG: String = ViewTimesListAdapter::class.java.simpleName

    private val mContext: Context
    private val mListTimes: MutableList<Times>
    private val mLayoutId: Int

    init {
        Log.d(LOG_TAG, "Creating list adapter")

        mContext = context
        mLayoutId = layoutId
        mListTimes = listTimes as MutableList<Times>
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        //Log.d(LOG_TAG, "Start getView position " + position);

        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // If convertView is not null, we can just reuse it from the recycler
        val cvtView = convertView ?: inflater.inflate(mLayoutId, parent, false)

        val times = mListTimes[position]
        val timeStart = Calendar.getInstance()
        timeStart.timeInMillis = times.timeStart
        val timeEnd = Calendar.getInstance()
        timeEnd.timeInMillis = times.timeEnd

        val dayOfMonth = cvtView.findViewById<TextView>(R.id.view_times_day_of_month)
        val dayOfWeek = cvtView.findViewById<TextView>(R.id.view_times_day_of_week)
        val monthAndYear = cvtView.findViewById<TextView>(R.id.view_times_month_and_year)
        val hours = cvtView.findViewById<TextView>(R.id.view_times_hours)
        val duration = cvtView.findViewById<TextView>(R.id.view_times_duration)
        val homeOffice = cvtView.findViewById<TextView>(R.id.view_times_homeoffice)

        dayOfMonth.text = String.format(Locale.getDefault(), "%td", timeStart)
        dayOfWeek.text = String.format(Locale.getDefault(), "%tA (%d)", timeStart, timeStart[Calendar.WEEK_OF_YEAR])
        monthAndYear.text = String.format(Locale.getDefault(), "%1\$tB %1\$tY", timeStart)
        hours.text = String.format(Locale.getDefault(), "%tR - %tR", timeStart, timeEnd)
        duration.text = String.format(Locale.getDefault(),
            "%s (%s)",
            formatTimeString24(getWorkedTime(mContext, timeStart, timeEnd, times.homeOffice)),
            formatTimeString24(getOverTime(mContext, timeStart, timeEnd, times.homeOffice)))
        homeOffice.visibility = if (times.homeOffice) View.VISIBLE else View.INVISIBLE

        return cvtView
    }

    /**
     * Remove object based on list id.
     *
     * If list is rebuild and object is from the list before, object.equals(a[i ]) in class.
     * ArrayList fails. So do the compare with the object.getId().
     *
     * Does **not** do a notifyDataSetChanged(), you have to do this manually!
     *
     * @param obj Times object
     */
    override
    fun remove(obj: Times?) {
        obj?.let {  // code inside the let block will only execute if obj is not null
            val index = mListTimes.indexOfFirst { it.id == obj.id }
            if (index != -1) {
                mListTimes.removeAt(index)
            }
        }
    }

    /**
     * Update object based on list id.
     * Does **not** do a notifyDataSetChanged(), you have to do this manually!
     *
     * @param obj Times object
     */
    fun update(obj: Times?) {
        obj?.let {  // code inside the let block will only execute if obj is not null
            val index = mListTimes.indexOfFirst { it.id == obj.id }
            if (index != -1) {
                mListTimes[index] = obj
            }
        }
    }


    /**
     * Sort the list
     */
    fun sort() {
        sort { a: Times, b: Times -> b.timeStart.compareTo(a.timeStart) }
    }

    /**
     * Refill the list. Does a notifyDataSetChanged().
     *
     * @param listTimes List of new times values
     */
    fun refill(listTimes: List<Times>) {
        mListTimes.clear()
        mListTimes.addAll(listTimes)
        notifyDataSetChanged()
    }
}