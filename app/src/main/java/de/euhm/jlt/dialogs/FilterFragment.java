/*
 * @name FilterFragment.java
 * @author hmueller
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Locale;

import de.euhm.jlt.R;
import de.euhm.jlt.utils.Constants;

/**
 * Fragment for editing the date filter
 * @author hmueller
 */
public class FilterFragment extends DialogFragment {

	public interface OnFilterFragmentListener {
		void onFinishFilterFragment(int id, int month, int year);
	}

	private OnFilterFragmentListener mListener;
	private static int mMonth = 0;
	private static int mYear = 0;

	public FilterFragment() {
		// create an empty constructor! No args allowed!
	}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFilterFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onFinishFilterFragment");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
    }

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if ((mYear == 0) || (mMonth == 0)) {
            throw new NullPointerException("FilterFragment class: setFilter() must be called before onCreateDialog()");
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
		final View view = inflater.inflate(R.layout.fragment_edit_filter, null);

		// show the times on the UI
		setPicker(view);

        builder.setView(view)
        	.setTitle(R.string.title_filter)
        	// Add action buttons
        	.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
        		@Override
        		public void onClick(DialogInterface dialog, int id) {
        			// handle Ok button
        			int month = getPickerMonth(view);
        			int year= getPickerYear(view);
        			// Return input to the listener/activity
        			mListener.onFinishFilterFragment(Constants.BUTTON_OK, month, year);
        		}
        	})
        	.setNegativeButton(R.string.button_clear, new DialogInterface.OnClickListener() {
        		@Override
        		public void onClick(DialogInterface dialog, int id) {
        			// handle clear filter button
    				// Return input to the listener/activity
    				mListener.onFinishFilterFragment(Constants.BUTTON_CLEAR, 0, 0);
        		}
        	});

        return builder.create();
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// clean up stored references to avoid leaking
		// do not clean static globals, otherwise rotating device crashes 
		mListener = null;
	}

	/**
	 * Set the month of the picker. Use current if 0.
	 * @param month Month to set.
	 */
	public void setPickerMonth(int month) {
		if (month == 0) {
			// if zero, use current month
			month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		}
		mMonth = month;
	}
	
	/**
	 * Set the year of the picker. Use current if 0.
	 * @param year Year to set.
	 */
	public void setPickerYear(int year) {
		if (year == 0) {
			// if zero, use current year
			year = Calendar.getInstance().get(Calendar.YEAR);
		}
		mYear = year;
	}

	/**
	 * Set the data in the picker view (month, year, limits)
	 * 
	 * @param view The current view of the picker
	 */
	private void setPicker(View view) {
		// set month picker
		NumberPicker monthPicker = view.findViewById(R.id.numberPicker_filter_month);
		monthPicker.setMinValue(1);
		monthPicker.setMaxValue(12);
		//monthPicker.setWrapSelectorWheel(false);
		//monthPicker.setFormatter(value -> String.format(Locale.getDefault(), "%02d", value));
		monthPicker.setFormatter(new NumberPicker.Formatter() {
		    @Override
		    public String format(int value) {
		        return String.format(Locale.getDefault(), "%02d", value);
		    }
		});
		monthPicker.setValue(mMonth);

		// set year picker
		NumberPicker yearPicker = view.findViewById(R.id.numberPicker_filter_year);
		yearPicker.setMinValue(1970);
		yearPicker.setMaxValue(2999);
		yearPicker.setWrapSelectorWheel(false);
		yearPicker.setValue(mYear);
	}

	/**
	 * Get the month of the filter picker
	 * 
	 * @param view Current view of the picker
	 * @return Selected month
	 */
	public static int getPickerMonth(View view) {
		// get month picker
		NumberPicker monthPicker = view.findViewById(R.id.numberPicker_filter_month);
		return monthPicker.getValue();
	}

	/**
	 * Get the year of the filter picker
	 * 
	 * @param view Current view of the picker
	 * @return Selected year
	 */
	public static int getPickerYear(View view) {
		// get year picker
		NumberPicker yearPicker = view.findViewById(R.id.numberPicker_filter_year);
		return yearPicker.getValue();
	}
}