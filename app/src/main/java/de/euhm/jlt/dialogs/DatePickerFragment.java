/**
 * @file DatePickerFragment.java
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

/**
 * Implements a DialogFragment for picking the date
 * @author hmueller
 */
public class DatePickerFragment extends DialogFragment
	implements DatePickerDialog.OnDateSetListener {

	public interface OnDatePickerFragmentListener {
		void onFinishDatePickerFragment(Calendar cal, int titleId);
	}

	private Calendar mCal = null; // selected date
	private int mTitleId = -1; // fragment title id
	private OnDatePickerFragmentListener mListener;
	
	public DatePickerFragment() {
		// create an empty constructor! No args allowed!
	}

	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof OnDatePickerFragmentListener) {
			mListener = (OnDatePickerFragmentListener) context; // Now it's safe to cast
		} else {
			throw new ClassCastException(context + " must implement OnDatePickerFragmentListener");
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (mCal == null) {
            throw new NullPointerException("DatePickerFragment class: set(Calender, int) must be called before onCreateDialog()");
		}

		// Use the current date as the default date in the picker
		int year = mCal.get(Calendar.YEAR);
		int month = mCal.get(Calendar.MONTH);
		int day = mCal.get(Calendar.DAY_OF_MONTH);

		// Create a new instance of DatePickerDialog and return it
		DatePickerDialog datePickerDialog = new DatePickerDialog(
			getActivity(), this, year, month, day);
		//  getActivity(), this, year, month, day);
		//datePickerDialog.setTitle(intTitleId);
		
		return datePickerDialog;
	}

	// Do something with the date chosen by the user
	@Override
	public void onDateSet(DatePicker view, int year, int month, int day) {
		if (view.isShown()) {
			// only update values if view is shown, avoids the problem that onDateSet
			// is called when the dialog is dismissed (e.g. by clicking outside), or called twice
			// http://stackoverflow.com/questions/19452993/ontimeset-called-also-when-dismissing-timepickerdialog
			mCal.set(Calendar.YEAR, year);
			mCal.set(Calendar.MONTH, month);
			mCal.set(Calendar.DAY_OF_MONTH, day);
			
			// Return input to the listener
			mListener.onFinishDatePickerFragment(mCal, mTitleId);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// clean up stored references to avoid leaking
		// do not clean static globals, otherwise rotating device crashes 
		mListener = null;
	}

	// get internal selected date
	public Calendar getCal() {
		return mCal;
	}

	// set internal date
	public void setCal(Calendar cal) {
		mCal = cal;
	}

	// set internal title id
	public void setTitel(int titleId) {
		mTitleId = titleId;
	}
	
	public void set(Calendar cal, int titleId) {
		if (cal == null) {
			// get current date and time
			mCal = Calendar.getInstance();
		} else {
			mCal = cal;
		}
		mTitleId = titleId;
	}

}
