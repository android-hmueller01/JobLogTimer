/**
 * @file EditTimesFragment.kt
 * 
 * based on http://www.vogella.com/tutorials/AndroidFragments/article.html
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import de.euhm.jlt.R
import de.euhm.jlt.dao.Times
import de.euhm.jlt.utils.Constants
import java.util.Calendar

/**
 * Fragment for editing a Times entry
 * @author Holger Mueller
 */
class EditTimesFragment : DialogFragment() {
    interface OnEditTimesFragmentListener {
        fun onFinishEditTimesFragment(id: Int, times: Times)
    }

    private lateinit var mListener: OnEditTimesFragmentListener
    private lateinit var mView: View
    private var mTimes: Times? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as OnEditTimesFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement onFinishEditTimesFragment")
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (mTimes == null) {
            throw NullPointerException("EditTimesFragment class: setTimes() must be called before onCreateDialog()")
        }

        val builder = AlertDialog.Builder(activity)
        //AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.edit_times_theme));
        // Get the layout inflater
        val inflater = requireActivity().layoutInflater

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        mView = inflater.inflate(R.layout.fragment_edit_times, null)

        // show the times on the UI
        setTimesPicker(mView, mTimes!!)

        builder.setView(mView) // Add action buttons
            .setPositiveButton(R.string.button_ok) { _, _ ->
                // handle Ok button
                val times = getTimesPicker(mView, mTimes!!)
                // Return input to the listener/activity
                mListener.onFinishEditTimesFragment(Constants.BUTTON_OK, times)
            }
            .setNegativeButton(R.string.button_cancel) { _, _ -> }
        if (mTimes!!.id != -1L) {
            builder.setView(mView)
                .setNeutralButton(R.string.button_delete) { _, _ ->
                    // handle neutral/delete button
                    // Return input to the listener/activity
                    mListener.onFinishEditTimesFragment(Constants.BUTTON_DELETE, mTimes!!)
                }
        }

        return builder.create()
    }

    fun setTimes(times: Times): EditTimesFragment {
        mTimes = times
        return this
    }

    /**
     * Set the data in the Date/Time Picker (date, start time, end time)
     *
     * @param view  The current view of the picker
     * @param times Times item defines in dao.times
     */
    private fun setTimesPicker(view: View, times: Times) {
        val cal = times.calStart
        val datePicker = view.findViewById<DatePicker>(R.id.datePicker1)
        datePicker.updateDate(cal[Calendar.YEAR], cal[Calendar.MONTH],
            cal[Calendar.DAY_OF_MONTH])

        var timePicker = view.findViewById<TimePicker>(R.id.timePickerStart)
        setTimePicker(timePicker, cal)

        cal.timeInMillis = times.timeEnd
        timePicker = view.findViewById(R.id.timePickerEnd)
        setTimePicker(timePicker, cal)

        val homeOfficeCb = view.findViewById<CheckBox>(R.id.homeoffice_cb)
        homeOfficeCb.isChecked = times.homeOffice
    }

    private fun setTimePicker(timePicker: TimePicker, cal: Calendar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.hour = cal[Calendar.HOUR_OF_DAY]
            timePicker.minute = cal[Calendar.MINUTE]
        } else {
            @Suppress("DEPRECATION")
            timePicker.currentHour = cal[Calendar.HOUR_OF_DAY]
            @Suppress("DEPRECATION")
            timePicker.currentMinute = cal[Calendar.MINUTE]
        }
        timePicker.setIs24HourView(true)
    }


    /**
     * Get the data of the Date/Time Picker (date, start time, end time)
     *
     * @param view  Current view of the picker
     * @param times Current times values
     * @return Times with updated values
     */
    private fun getTimesPicker(view: View, times: Times): Times {
        val cal = Calendar.getInstance()

        // get start time
        cal.timeInMillis = times.timeStart
        val datePicker = view.findViewById<DatePicker>(R.id.datePicker1)
        cal[Calendar.DAY_OF_MONTH] = datePicker.dayOfMonth
        cal[Calendar.MONTH] = datePicker.month
        cal[Calendar.YEAR] = datePicker.year

        // set start time
        getTimePicker(view.findViewById(R.id.timePickerStart), cal)
        times.timeStart = cal.timeInMillis

        // set end time
        getTimePicker(view.findViewById(R.id.timePickerEnd), cal)
        times.timeEnd = cal.timeInMillis

        // get home office setting
        val homeOfficeCb = view.findViewById<CheckBox>(R.id.homeoffice_cb)
        times.homeOffice = homeOfficeCb.isChecked

        return times
    }

    private fun getTimePicker(timePicker: TimePicker, cal: Calendar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cal[Calendar.HOUR_OF_DAY] = timePicker.hour
            cal[Calendar.MINUTE] = timePicker.minute
        } else {
            @Suppress("DEPRECATION")
            cal[Calendar.HOUR_OF_DAY] = timePicker.currentHour
            @Suppress("DEPRECATION")
            cal[Calendar.MINUTE] = timePicker.currentMinute
        }
    }
}