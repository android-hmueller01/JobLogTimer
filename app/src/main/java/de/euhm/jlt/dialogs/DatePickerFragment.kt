/**
 * @file DatePickerFragment.kt
 *
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.dialogs

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

/**
 * Implements a DialogFragment for picking the date
 * @author hmueller
 */
class DatePickerFragment : DialogFragment(), OnDateSetListener {
    interface OnDatePickerFragmentListener {
        fun onFinishDatePickerFragment(cal: Calendar, titleId: Int)
    }

    // get / set internal date
    @Suppress("MemberVisibilityCanBePrivate")
    var cal: Calendar = Calendar.getInstance()  // selected date
    private var mTitleId = -1  // fragment title id
    private lateinit var mListener: OnDatePickerFragmentListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnDatePickerFragmentListener) {
            mListener = context // Now it's safe to cast
        } else {
            throw ClassCastException("$context must implement OnDatePickerFragmentListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val year = cal[Calendar.YEAR]
        val month = cal[Calendar.MONTH]
        val day = cal[Calendar.DAY_OF_MONTH]

        // Create a new instance of DatePickerDialog and return it
        val datePickerDialog = DatePickerDialog(requireActivity(), this, year, month, day)

        datePickerDialog.setTitle(mTitleId)
        return datePickerDialog
    }

    // Do something with the date chosen by the user
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        if (view.isShown) {
            // only update values if view is shown, avoids the problem that onDateSet
            // is called when the dialog is dismissed (e.g. by clicking outside), or called twice
            // http://stackoverflow.com/questions/19452993/ontimeset-called-also-when-dismissing-timepickerdialog
            cal[Calendar.YEAR] = year
            cal[Calendar.MONTH] = month
            cal[Calendar.DAY_OF_MONTH] = day

            // Return input to the listener
            mListener.onFinishDatePickerFragment(cal, mTitleId)
        }
    }

    // set internal title id
    @Suppress("unused")
    fun setTitel(titleId: Int): DatePickerFragment {
        mTitleId = titleId
        return this
    }

    fun set(cal: Calendar?, titleId: Int): DatePickerFragment {
        this.cal = cal ?: Calendar.getInstance()  // get current date and time if cal is null
        mTitleId = titleId
        return this
    }
}
