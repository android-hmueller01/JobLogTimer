/**
 * @file TimePickerFragment.kt
 * @author Holger Mueller
 * 
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.dialogs

import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

/**
 * Implements a DialogFragment for picking the time
 * @author Holger Mueller
 */
class TimePickerFragment : DialogFragment(), OnTimeSetListener {
    interface OnTimePickerFragmentListener {
        fun onFinishTimePickerFragment(cal: Calendar, titleId: Int)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    var cal: Calendar = Calendar.getInstance() // selected time
    private var mTitleId = -1 // fragment title id
    private lateinit var mListener: OnTimePickerFragmentListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as OnTimePickerFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnTimePickerFragmentListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val hour = cal[Calendar.HOUR_OF_DAY]
        val minute = cal[Calendar.MINUTE]

        // Create a new instance of TimePickerDialog and return it
        val timePickerDialog = TimePickerDialog(
            requireActivity(), this, hour, minute,
            DateFormat.is24HourFormat(requireActivity()))
        timePickerDialog.setTitle(mTitleId)
        return timePickerDialog
    }

    // Do something with the time chosen by the user
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        if (view.isShown) {
            // only update values if view is shown, avoids the problem that onTimeSet
            // is called when the dialog is dismissed (e.g. by clicking outside), or called twice
            // http://stackoverflow.com/questions/19452993/ontimeset-called-also-when-dismissing-timepickerdialog
            cal[Calendar.HOUR_OF_DAY] = hourOfDay
            cal[Calendar.MINUTE] = minute
            cal[Calendar.SECOND] = 0

            // Return input to activity
            mListener.onFinishTimePickerFragment(cal, mTitleId)
        }
    }

    // set internal title id
    @Suppress("unused")
    fun setTitle(titleId: Int): TimePickerFragment {
        mTitleId = titleId
        return this
    }

    fun set(cal: Calendar?, titleId: Int): TimePickerFragment {
        this.cal = cal ?: Calendar.getInstance() // get current date and time if cal is null
        mTitleId = titleId
        return this
    }
}
