/**
 * @file FilterFragment.kt
 *
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import de.euhm.jlt.R
import de.euhm.jlt.utils.Constants
import java.util.Calendar
import java.util.Locale

/**
 * Fragment for editing the date filter
 * @author hmueller
 */
class FilterFragment : DialogFragment() {
    interface OnFilterFragmentListener {
        fun onFinishFilterFragment(id: Int, month: Int, year: Int)
    }

    private lateinit var mListener: OnFilterFragmentListener
    private var mMonth = 0
    private var mYear = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as OnFilterFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement onFinishFilterFragment")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if ((mYear == 0) || (mMonth == 0)) {
            throw NullPointerException("FilterFragment class: setFilter() must be called before onCreateDialog()")
        }

        val builder = AlertDialog.Builder(requireActivity())
        // Get the layout inflater
        val inflater = requireActivity().layoutInflater

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        val view = inflater.inflate(R.layout.fragment_edit_filter, null)

        // show the times on the UI
        setPicker(view)

        builder.setView(view).setTitle(R.string.title_filter) // Add action buttons
            .setPositiveButton(R.string.button_ok) { _, _ ->
                // handle Ok button
                val month = getPickerMonth(view)
                val year = getPickerYear(view)
                // Return input to the listener/activity
                mListener.onFinishFilterFragment(Constants.BUTTON_OK, month, year)
            }.setNegativeButton(R.string.button_clear) { _, _ ->
                // handle clear filter button
                // Return input to the listener/activity
                mListener.onFinishFilterFragment(Constants.BUTTON_CLEAR, 0, 0)
            }

        return builder.create()
    }

    /**
     * Set the month of the picker. Use current if 0.
     *
     * @param month The month to set (1-12), or 0 to use the current month.
     * @return This [FilterFragment] instance for chaining.
     * @throws IllegalArgumentException if month is outside the range of 0-12.
     */
    fun setPickerMonth(month: Int): FilterFragment {
        require(month in 0..12) { "Month must be between 0 and 12." }
        mMonth = if (month == 0) {
            // Use current month if 0 is provided.
            Calendar.getInstance()[Calendar.MONTH] + 1
        } else {
            month
        }
        return this
    }

    /**
     * Set the year of the picker. Use current if 0.
     *
     * @param year Year to set.
     * @return This [FilterFragment] instance for chaining.
     */
    fun setPickerYear(year: Int): FilterFragment {
        mYear = if (year == 0) {
            // if zero, use current year
            Calendar.getInstance()[Calendar.YEAR]
        } else {
            year
        }
        return this
    }

    /**
     * Set the data in the picker view (month, year, limits)
     *
     * @param view The current view of the picker
     */
    private fun setPicker(view: View) {
        // set month picker
        val monthPicker = view.findViewById<NumberPicker>(R.id.numberPicker_filter_month)
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        //monthPicker.setWrapSelectorWheel(false);
        monthPicker.setFormatter { value: Int ->
            String.format(Locale.getDefault(), "%02d", value)
        }
        monthPicker.value = mMonth

        // set year picker
        val yearPicker = view.findViewById<NumberPicker>(R.id.numberPicker_filter_year)
        yearPicker.minValue = 1970
        yearPicker.maxValue = 2999
        yearPicker.wrapSelectorWheel = false
        yearPicker.value = mYear
    }

    /**
     * Get the month of the filter picker
     *
     * @param view Current view of the picker
     * @return Selected month
     */
    private fun getPickerMonth(view: View): Int {
        // get month picker
        val monthPicker = view.findViewById<NumberPicker>(R.id.numberPicker_filter_month)
        return monthPicker.value
    }

    /**
     * Get the year of the filter picker
     *
     * @param view Current view of the picker
     * @return Selected year
     */
    private fun getPickerYear(view: View): Int {
        // get year picker
        val yearPicker = view.findViewById<NumberPicker>(R.id.numberPicker_filter_year)
        return yearPicker.value
    }
}