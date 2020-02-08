/**
 * $Id: EditTimesFragment.java 187 2016-12-22 18:10:54Z hmueller $
 * 
 * based on http://www.vogella.com/tutorials/AndroidFragments/article.html
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.dialogs;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

import de.euhm.jlt.R;
import de.euhm.jlt.dao.Times;
import de.euhm.jlt.utils.Constants;

/**
 * Fragment for editing a Times entry
 * @author hmueller
 * @version $Rev: 187 $
 */
public class EditTimesFragment extends DialogFragment {

	public interface OnEditTimesFragmentListener {
		void onFinishEditTimesFragment(int id, Times times);
	}

	private OnEditTimesFragmentListener mListener;
	private static View mView;
	private static Times mTimes;

	public EditTimesFragment() {
		// create an empty constructor! No args allowed!
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnEditTimesFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onFinishEditTimesFragment");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
    }
    
    @SuppressLint("InflateParams")
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (mTimes == null) {
            throw new NullPointerException("EditTimesFragment class: setTimes() must be called before onCreateDialog()");
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.edit_times_theme));
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        mView = inflater.inflate(R.layout.fragment_edit_times, null);

		// show the times on the UI
		setTimesPicker(mView, mTimes);

        builder.setView(mView)
        	// Add action buttons
        	.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
        		@Override
        		public void onClick(DialogInterface dialog, int id) {
        			// handle Ok button
        			Times times = getTimesPicker(mView, mTimes);
        			// Return input to the listener/activity
        			mListener.onFinishEditTimesFragment(Constants.BUTTON_OK, times);
        		}
        	})
        	.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
        		@Override
        		public void onClick(DialogInterface dialog, int id) {
        			// handle cancel button
        		}
        	});
        if (mTimes.getId() != -1) {
        	builder.setView(mView)
        		.setNeutralButton(R.string.button_delete, new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface dialog, int id) {
        				// handle neutral/delete button
        				// Return input to the listener/activity
        				mListener.onFinishEditTimesFragment(Constants.BUTTON_DELETE, mTimes);
        			}
        		});
        }

        return builder.create();
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// clean up stored references to avoid leaking
		// do not clean static globals, otherwise rotating device crashes 
		mListener = null;
	}

	public void setTimes(Times times) {
		mTimes = times;
	}
	
	/**
	 * Set the data in the Date/Time Picker (date, start time, end time)
	 * 
	 * @param view
	 *            The current view of the picker
	 * @param times
	 *            Times item defines in dao.times
	 */
	@TargetApi(23)
	@SuppressWarnings("deprecation")
	private void setTimesPicker(View view, Times times) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(times.getTimeStart());
		DatePicker datePicker = (DatePicker) view
				.findViewById(R.id.datePicker1);
		datePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
				cal.get(Calendar.DAY_OF_MONTH));

		TimePicker timePicker = (TimePicker) view
				.findViewById(R.id.timePickerStart);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    		timePicker.setHour(cal.get(Calendar.HOUR_OF_DAY));
    		timePicker.setMinute(cal.get(Calendar.MINUTE));
        } else {
    		timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
    		timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
        }
		timePicker.setIs24HourView(true);

		cal.setTimeInMillis(times.getTimeEnd());
		timePicker = (TimePicker) view.findViewById(R.id.timePickerEnd);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        	timePicker.setHour(cal.get(Calendar.HOUR_OF_DAY));
        	timePicker.setMinute(cal.get(Calendar.MINUTE));
        } else {
        	timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
        	timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
        }
		timePicker.setIs24HourView(true);
	}

	/**
	 * Get the data of the Date/Time Picker (date, start time, end time)
	 * 
	 * @param view
	 *            Current view of the picker
	 * @param times
	 *            Current times values
	 * @return Times with updated values
	 */
	@TargetApi(23)
	@SuppressWarnings("deprecation")
	public static Times getTimesPicker(View view, Times times) {
		Calendar cal = Calendar.getInstance();

		// get start time
		cal.setTimeInMillis(times.getTimeStart());
		DatePicker datePicker = (DatePicker) view
				.findViewById(R.id.datePicker1);
		cal.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
		cal.set(Calendar.MONTH, datePicker.getMonth());
		cal.set(Calendar.YEAR, datePicker.getYear());

		TimePicker timePicker = (TimePicker) view
				.findViewById(R.id.timePickerStart);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        	cal.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
        	cal.set(Calendar.MINUTE, timePicker.getMinute());
        } else {
    		cal.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
    		cal.set(Calendar.MINUTE, timePicker.getCurrentMinute());
        }
		// set start time
		times.setTimeStart(cal.getTimeInMillis());

		// get end time
		timePicker = (TimePicker) view.findViewById(R.id.timePickerEnd);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        	cal.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
        	cal.set(Calendar.MINUTE, timePicker.getMinute());
        } else {
        	cal.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
        	cal.set(Calendar.MINUTE, timePicker.getCurrentMinute());
        }
		// set end time
		times.setTimeEnd(cal.getTimeInMillis());

		return times;
	}

}