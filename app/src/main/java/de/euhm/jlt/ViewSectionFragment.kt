/**
 * @file ViewSectionFragment.kt
 *
 * based on http://www.vogella.com/tutorials/AndroidSQLite/article.html#databasetutorial
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
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.ListFragment
import androidx.viewpager2.widget.ViewPager2
import de.euhm.jlt.dao.Times
import de.euhm.jlt.dao.TimesDataSource
import de.euhm.jlt.dao.TimesWork
import de.euhm.jlt.dialogs.EditTimesFragment
import de.euhm.jlt.dialogs.EditTimesFragment.OnEditTimesFragmentListener
import de.euhm.jlt.listadapter.ViewTimesListAdapter
import de.euhm.jlt.utils.Constants
import de.euhm.jlt.utils.Prefs
import de.euhm.jlt.utils.TimeUtil
import java.util.Calendar
import java.util.Locale

/**
 * Fragment for viewing the times database
 *
 * @author hmueller
 */
class ViewSectionFragment : ListFragment() {
    @Suppress("PrivatePropertyName")
    private val LOG_TAG: String = ViewSectionFragment::class.java.simpleName
    private lateinit var mDatasource: TimesDataSource // gets used outside the normal initialization
    private lateinit var mContext: Context // gets initialized in onAttach()
    private lateinit var mListView: ListView
    private lateinit var mViewPager: ViewPager2

    /**
     * A [BroadcastReceiver] to update the view in this fragment.<br></br>
     * Register in onCreate() and unregister in onDestroy()!
     */
    private val receiverUpdateView: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.v(LOG_TAG, "Received BroadcastReceiver " + Constants.RECEIVER_UPDATE_VIEW)
            // update ListAdapter and info line at top
            val adapter = listAdapter as? ViewTimesListAdapter?
            adapter?.refill(getListTimes())
            updateInfoLine()
        }
    }

    init {
        // save this in static MainActivity variable for later use, because
        // class gets reinstated outside MainActivity e.g. by rotating the device
        MainActivity.mViewSectionFragment = this
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get Times database
        mDatasource = TimesDataSource(mContext)

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
        val view = inflater.inflate(R.layout.fragment_section_view, container, false)
        val listTimes = getListTimes()

        // use the ViewTimesListAdapter to show the elements in a ListView
        listAdapter = ViewTimesListAdapter(mContext, R.layout.list_item_view_times, listTimes)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateInfoLine()

        // get the viewpager of the tabLayout toolbar
        mViewPager = requireActivity().findViewById(R.id.pager)

        // dynamically hide and show ActionBar
        mListView = listView
        mListView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
            }

            var lastVisibleItem: Int = 0 // remember last firstVisibleItem in onScroll()

            override fun onScroll(view: AbsListView,
                                  firstVisibleItem: Int,
                                  visibleItemCount: Int,
                                  totalItemCount: Int) {
                val actionBar = (requireActivity() as? AppCompatActivity)?.supportActionBar
                if (view.id == mListView.id && mViewPager.currentItem == 1 && actionBar != null) {
                    if (totalItemCount > 0) {//(visibleItemCount * 2 < totalItemCount) {
                        // hide ActionBar only, if we have at least double entries than visible entries
                        if (firstVisibleItem > lastVisibleItem + 1) {
                            // list is scrolled down (finger moved up)
                            actionBar.hide()
                            lastVisibleItem = firstVisibleItem
                        } else if (firstVisibleItem < lastVisibleItem - 1) {
                            // list is scrolled up (finger moved down)
                            actionBar.show()
                            lastVisibleItem = firstVisibleItem
                        }
                    }
                }
            }
        })
        // handle item clicks (edit item)
        mListView.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, pos: Int, _: Long ->
            Log.d(LOG_TAG, "onListItemClick, pos=$pos")
            val frag = EditTimesFragment().setTimes((mListView.getItemAtPosition(pos) as Times))
            // Display the edit fragment as the main content.
            childFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(0, frag).commit()
        }
        // handle long press clicks (delete item)
        // Do NOT set android:longClickable="true" in XML!
        // @param parent The AbsListView where the click happened
        // @param view The view within the AbsListView that was clicked
        // @param pos The position of the view in the list
        // @param id The row id of the item that was clicked
        // @return true if the callback consumed the long click, false otherwise
        mListView.onItemLongClickListener = OnItemLongClickListener { _: AdapterView<*>?, _: View?, pos: Int, _: Long ->
            Log.d(LOG_TAG, "onItemLongClick, pos=$pos")
            val times = mListView.getItemAtPosition(pos) as Times
            (requireActivity() as OnEditTimesFragmentListener).onFinishEditTimesFragment(Constants.BUTTON_DELETE, times)
            true // handle as long click, otherwise this will be handled as normal click
        }
    }

    /**
     * Get list of times depending on filter
     *
     * @return list of times
     */
    private fun getListTimes(): List<Times> {
        val listTimes: List<Times>

        // build list
        if (TimesWork.filterMonth == 0 || TimesWork.filterYear == 0) {
            // get all data
            listTimes = mDatasource.allTimes
        } else {
            // get filtered range
            val cal = TimeUtil.getFilterCal(TimesWork.filterMonth, TimesWork.filterYear)
            val timeStart = cal.timeInMillis
            // get end of month
            cal[Calendar.DAY_OF_MONTH] = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            // really set the end of the day, otherwise the last day will not be shown on the last day
            cal[Calendar.HOUR_OF_DAY] = 23
            cal[Calendar.MINUTE] = 59
            val timeEnd = cal.timeInMillis
            listTimes = mDatasource.getTimeRangeTimes(timeStart, timeEnd)
        }
        return listTimes
    }

    /**
     * Update summary info line
     */
    private fun updateInfoLine() {
        // get views of times info line
        val viewInfoLineLeft = requireView().findViewById<TextView>(R.id.view_times_info_line_left)
        val viewInfoLineMiddle = requireView().findViewById<TextView>(R.id.view_times_info_line_middle)
        val viewInfoLineRight = requireView().findViewById<TextView>(R.id.view_times_info_line_right)

        // set default values (if we have leave before setting at the end)
        val strLeft = String.format(Locale.getDefault(), resources.getString(R.string.view_times_info_line_left), 0)
        var strMiddle = ""
        var strFilterActive = ""
        if (TimesWork.filterMonth != 0 && TimesWork.filterYear != 0) {
            // filter is active, indicate that
            strFilterActive = " " + resources.getString(R.string.view_times_info_line_filter_active)
            val cal = TimeUtil.getFilterCal(TimesWork.filterMonth, TimesWork.filterYear)
            strMiddle = String.format(Locale.getDefault(),
                "%s %04d",
                DateFormat.format("MMM", cal.timeInMillis),
                TimesWork.filterYear)
        }
        viewInfoLineLeft.text = String.format("%s%s", strLeft, strFilterActive)
        viewInfoLineMiddle.text = strMiddle
        viewInfoLineRight.text = ""

        // get filtered list (if available) and calculate times
        val la = listAdapter ?: return
        val cnt = la.count
        if (cnt == 0) return
        var workedTime: Long = 0
        var overTime: Long = 0
        var workedPerDay: Long = 0 // worked time per day, to support multiple entries on the same day
        val prefs = Prefs(mContext)
        for (i in 0..<cnt) {
            val ti = la.getItem(i) as Times
            // do only calc worked time and not overtime, as we do not know jet, if we have more entries on the same day
            workedPerDay += TimeUtil.getWorkedTime(mContext, ti.timeStart, ti.timeEnd, ti.homeOffice)
            // do we have a next value?
            val tiNext = if (i + 1 < cnt) {
                // yes, use that
                la.getItem(i + 1) as Times
            } else {
                // no, set day to next day (which is != current day) to finish calculating this day (see if below)
                Times(0, ti.timeStart + 24 * 60 * 60 * 1000, 0, ti.homeOffice)
            }
            // do we have more values with same day?
            if (ti.calStart[Calendar.DAY_OF_MONTH] != tiNext.calStart[Calendar.DAY_OF_MONTH]) {
                // no, finish calculating this day
                workedTime += workedPerDay
                overTime += workedPerDay - prefs.hoursInMillis
                workedPerDay = 0
            }
        }

        // update info line with calculated values
        viewInfoLineLeft.text = String.format(Locale.getDefault(),
            resources.getString(R.string.view_times_info_line_left) + strFilterActive,
            cnt)
        viewInfoLineMiddle.text = String.format(Locale.getDefault(),
            resources.getString(R.string.view_times_info_line_middle),
            (la.getItem(cnt - 1) as Times).dateString,
            (la.getItem(0) as Times).dateString)
        viewInfoLineRight.text = String.format(Locale.getDefault(),
            resources.getString(R.string.view_times_info_line_right),
            TimeUtil.formatTimeString(workedTime),
            TimeUtil.formatTimeString(overTime))
    }

    fun addTimesItem(times: Times) {
        mDatasource.createTimes(times)
        val adapter = listAdapter as? ViewTimesListAdapter?
        adapter?.setNotifyOnChange(false) // do not notify changes yet
        adapter?.add(times)
        adapter?.sort()
        adapter?.notifyDataSetChanged() // now update the changed data
        updateInfoLine()
    }

    fun updateTimesItem(times: Times?) {
        if (times == null) {
            Toast.makeText(mContext, "updateTimesItem: times is null! Exit!", Toast.LENGTH_LONG).show()
            return
        }

        val rows = mDatasource.updateTimes(times)
        if (rows != 1) {
            Toast.makeText(mContext, "Database update failed. rows should be 1 but is $rows", Toast.LENGTH_LONG).show()
        }
        // only needed,  if list was recreated in the meantime (e.g. by rotating the device)
        val adapter = listAdapter as? ViewTimesListAdapter?
        adapter?.setNotifyOnChange(false) // do not notify changes yet
        adapter?.update(times)
        adapter?.sort()
        adapter?.notifyDataSetChanged() // now update the changed data
        updateInfoLine()
    }

    fun deleteTimesItem(times: Times?) {
        if (times == null) {
            Toast.makeText(mContext, "deleteTimesItem: times is null! Exit!", Toast.LENGTH_LONG).show()
            return
        }

        val rows = mDatasource.deleteTimes(times)
        if (rows != 1) {
            Toast.makeText(mContext, "Database delete failed. rows should be 1 but is $rows", Toast.LENGTH_LONG).show()
        }

        val adapter = listAdapter as? ViewTimesListAdapter?
        adapter?.remove(times) // remove item from adapter list
        adapter?.notifyDataSetChanged() // now update the changed data
        updateInfoLine()
    }

    fun updateFilter(month: Int, year: Int) {
        TimesWork.filterMonth = month
        TimesWork.filterYear = year

        // update ListAdapter
        val adapter = listAdapter as? ViewTimesListAdapter?
        adapter?.refill(getListTimes())
        updateInfoLine()
    }

    override fun onPause() {
        super.onPause()
        mDatasource.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        mContext.unregisterReceiver(receiverUpdateView)
    }
}