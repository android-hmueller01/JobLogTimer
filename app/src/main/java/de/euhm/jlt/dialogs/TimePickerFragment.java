/*
 * @file TimePickerFragment.java
 * @author Holger Mueller
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

/**
 * Implements a DialogFragment for picking the time
 * @author Holger Mueller
 */
public class TimePickerFragment extends DialogFragment
	implements TimePickerDialog.OnTimeSetListener {

	public interface OnTimePickerFragmentListener {
		void onFinishTimePickerFragment(Calendar cal, int titleId);
	}

	private static Calendar mCal = null; // selected time
	private static int mTitleId = -1; // fragment title id
	private OnTimePickerFragmentListener mListener;

	public TimePickerFragment() {
		// create an empty constructor! No args allowed!
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		try {
			mListener = (OnTimePickerFragmentListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement OnTimePickerFragmentListener");
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (mCal == null) {
			throw new NullPointerException("TimePickerFragment class: set(Calender, int) must be called before onCreateDialog()");
		}

		// Use the current time as the default values for the picker
		int hour = mCal.get(Calendar.HOUR_OF_DAY);
		int minute = mCal.get(Calendar.MINUTE);

		// Create a new instance of TimePickerDialog and return it
		TimePickerDialog timePickerDialog = new TimePickerDialog(
			getActivity(), this, hour, minute,
			DateFormat.is24HourFormat(getActivity()));
		timePickerDialog.setTitle(mTitleId);
		/*
		timePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, 
			getString(R.string.button_ok), 
			(OnClickListener) this);
		timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, 
			getString(R.string.button_cancel), 
			(OnClickListener) this);
		*/

		return timePickerDialog;
	}

	// Do something with the time chosen by the user
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		if (view.isShown()) {
			// only update values if view is shown, avoids the problem that onTimeSet
			// is called when the dialog is dismissed (e.g. by clicking outside), or called twice
			// http://stackoverflow.com/questions/19452993/ontimeset-called-also-when-dismissing-timepickerdialog
			mCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
			mCal.set(Calendar.MINUTE, minute);
			mCal.set(Calendar.SECOND, 0);
			
			// Return input to activity
			mListener.onFinishTimePickerFragment(mCal, mTitleId);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// clean up stored references to avoid leaking
		// do not clean static globals, otherwise rotating device crashes 
		mListener = null;
	}

	// get internal selected time
	public Calendar get() {
		return mCal;
	}

	// set internal time
	public void set(Calendar cal) {
		mCal = cal;
	}

	// set internal title id
	public void setTitle(int titleId) {
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
