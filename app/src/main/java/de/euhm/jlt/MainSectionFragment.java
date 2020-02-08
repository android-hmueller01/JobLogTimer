/*
 * @file MainSectionFragment.java
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.euhm.jlt.dao.Times;
import de.euhm.jlt.dao.TimesDataSource;
import de.euhm.jlt.dao.TimesWork;
import de.euhm.jlt.preferences.Prefs;
import de.euhm.jlt.utils.Constants;
import de.euhm.jlt.utils.LongRef;
import de.euhm.jlt.utils.TimeUtil;

/**
 * Main section fragment of JobLog
 * @author hmueller
 */
public class MainSectionFragment extends Fragment {
	private final String LOG_TAG = MainSectionFragment.class.getSimpleName();
	private Context mContext; // gets initialized in onAttach()
	private TimesWork mTW; // gets initialized in onCreate()
	private Handler mHandlerUpdateTimes = new Handler();
	private final long HANDLER_UPDATE_TIMES_DELAY = 1 * 1000;
	private OnSwipeTouchListener mOnSwipeTouchListener;
	
	/**
	 * A {@link BroadcastReceiver} to update the view in this fragment.<br>
	 * Register in onCreate() and unregister in onDestroy()!
	 */
	private final BroadcastReceiver receiverUpdateView = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
			Log.v(LOG_TAG, "Received BroadcastReceiver " + Constants.RECEIVER_UPDATE_VIEW);
			// update times view and statistics
    		updateTimesView();
    		updateStatisticsView();
        }
	};

	public MainSectionFragment() {
		// save this in static MainActivity variable for later use, because
		// class gets reinstated outside MainActivity e.g. by rotating the device
		MainActivity.mMainSectionFragment = this;
	}

	@Override
	public void onAttach(Context context) {
	    super.onAttach(context);
	    mContext = context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		// get current Work Times DAO
		mTW = new TimesWork(mContext);

		// register the update view receiver to update view from service
	    mContext.registerReceiver(receiverUpdateView, new IntentFilter(Constants.RECEIVER_UPDATE_VIEW));
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_section_main, container, false);

		// setup the left/right main statistics swipe gesture
		mOnSwipeTouchListener = new OnSwipeTouchListener(mContext, 
				view.findViewById(R.id.layout_main_statistics)) {
		    @Override
		    public boolean onSwipeRight() {
                //Toast.makeText(mContext, "Right swipe [Previous]", Toast.LENGTH_SHORT).show();
                Calendar cal = mTW.getStatisticsDate();
                cal.add(Calendar.WEEK_OF_YEAR, -1);
                updateStatisticsView();
				return true;
		    }
		    @Override
		    public boolean onSwipeLeft() {
                //Toast.makeText(mContext, "Left swipe [Next]", Toast.LENGTH_SHORT).show();
		    	boolean result = false;
		    	
                Calendar cal = mTW.getStatisticsDate();
                Calendar now = Calendar.getInstance();
                // do not change cal before we checked if user wants to swipe after current
                // date, because mTW.getStatisticsDate() returns a static reference!
                now.add(Calendar.WEEK_OF_YEAR, -1);
                if (cal.before(now)) {
                    cal.add(Calendar.WEEK_OF_YEAR, 1);
                	updateStatisticsView();
                	result = true;
                }
		    	return result;
		    }
		    @Override
		    public boolean onDoubleTap() {
                //Toast.makeText(mContext, "Double tap [Current Date]", Toast.LENGTH_SHORT).show();
      		  	mTW.setStatisticsDate(TimeUtil.getCurrentTime());
                updateStatisticsView();
				return true;
		    }
		};

		return view;
	}

	/**
	 * @return the mOnSwipeTouchListener
	 */
	public OnSwipeTouchListener getOnSwipeTouchListener() {
		return mOnSwipeTouchListener;
	}

	@SuppressLint("SetTextI18n")
	public void updateTimesView() {
		View view = getView();
		// only update the view if all global variables are valid ...
		if ((view == null) || (mTW == null)) {
			Log.d(LOG_TAG, "view or mTW is null. Not executing updateTimesView() ...");
			return;
		}

		TextView tv;
		View v;
		RelativeLayout.LayoutParams params;
		Prefs prefs = new Prefs(mContext);
		long curTimeMillis = TimeUtil.getCurrentTimeInMillis();

		if (!mTW.getWorkStarted() && (mTW.getTimeStart() != -1) && (mTW.getTimeEnd() != -1)) {
			// work is done and times are set
			curTimeMillis = mTW.getTimeEnd();
		}

		// update current time in seconds
		tv = view.findViewById(R.id.current_time);
		tv.setText(String.format(Locale.getDefault(),
				getResources().getString(R.string.current_time_text), Calendar.getInstance()));

		// calculate the percentage worked
		float percentCurrent = (float) (curTimeMillis - mTW.getTimeStart()) / 
				(float) (mTW.getNormalWorkEndTime() - mTW.getTimeStart());
		float percentNormal = (float) (mTW.getNormalWorkEndTime() - mTW.getTimeStart()) / 
				(float) (mTW.getMaxWorkEndTime() - mTW.getTimeStart());
		float percentProgressBar = (float) (curTimeMillis - mTW.getTimeStart()) / 
				(float) (mTW.getMaxWorkEndTime() - mTW.getTimeStart());
		
		TextView tvDate = view.findViewById(R.id.date_val);
		TextView tvTime = view.findViewById(R.id.start_val);
		ProgressBar progressBar = view.findViewById(R.id.progress_bar);
		if (mTW.getTimeStart() == -1) {
			tvDate.setText(" --.--.---- ");
			tvTime.setText(" --:-- ");
			progressBar.setSecondaryProgress(0);
			progressBar.setProgress(0);
			tv = view.findViewById(R.id.progress_bar_percent_val);
			tv.setText("-");
			tv = view.findViewById(R.id.progress_bar_time_1);
			tv.setText("-");
			tv = view.findViewById(R.id.progress_bar_time_2);
			tv.setText("-");
			tv = view.findViewById(R.id.progress_bar_time_3);
			tv.setText("-");
		} else {
			// view start date and time
			tvDate.setText(String.format(Locale.getDefault(), " %1$td.%1$tm.%1$tY ", mTW.getCalStart()));
			tvTime.setText(String.format(Locale.getDefault(), " %tR ", mTW.getCalStart()));

			// set the progress bar values
			String workedTimeString = TimeUtil.formatTimeString(TimeUtil.getWorkedTime(mContext, mTW.getTimeStart(), curTimeMillis));
			String addProgressBarInfo;
			int progress;
			int secondaryProgress;
			if (curTimeMillis > mTW.getNormalWorkEndTime()) {
				// we are in over time ...
				progress = (int) Math.floor(percentNormal * 100);
				secondaryProgress = (int) Math.floor(percentProgressBar * 100);
				addProgressBarInfo = "(" + TimeUtil.formatTimeString(
						TimeUtil.getOverTime(mContext, mTW.getTimeStart(), curTimeMillis)) + ")";
			} else {
				// we are in normal work time, no secondaryProgress needed
				progress = (int) Math.floor(percentProgressBar * 100);
				secondaryProgress = 0;
				if (prefs.getViewPercentEnabled()) {
					addProgressBarInfo = "(" + (int) Math.floor(percentCurrent * 100) + "%)";
				} else {
					addProgressBarInfo = "(" + TimeUtil.formatTimeString(
						TimeUtil.getOverTime(mContext, mTW.getTimeStart(), curTimeMillis)) + ")";
				}
			}
			progressBar.setSecondaryProgress(secondaryProgress);
			progressBar.setProgress(progress);
			
			// set the progress bar text
			tv = view.findViewById(R.id.progress_bar_percent_val);
			tv.setText(workedTimeString + " " + addProgressBarInfo);
			
			// set tick 1 values
			tv = view.findViewById(R.id.progress_bar_time_1);
			tv.setText(String.format(Locale.getDefault(), "%tR", mTW.getCalStart()));
			
			// set tick 2 values
			v = view.findViewById(R.id.progress_bar_tick_2);
			int pos = (int) Math.floor(percentNormal * progressBar.getWidth());
			//v.setX(progressBarLeft + pos - v.getWidth()); // API Level 11 needed -> use setLayoutParams()
			params = (RelativeLayout.LayoutParams) v.getLayoutParams();
			params.leftMargin = pos - v.getWidth() / 2;
			v.setLayoutParams(params);
			tv = view.findViewById(R.id.progress_bar_time_2);
			tv.setText(String.format(Locale.getDefault(), "%tR", mTW.getCalNormalWorkEndTime()));
			params = (RelativeLayout.LayoutParams) tv.getLayoutParams();
			params.leftMargin = pos - tv.getWidth() / 2;
			tv.setLayoutParams(params);
			
			// set tick 3 values
			tv = view.findViewById(R.id.progress_bar_time_3);
			tv.setText(String.format(Locale.getDefault(), "%tR", mTW.getCalMaxWorkEndTime()));
		}

		tv = view.findViewById(R.id.end_val);
		if (mTW.getTimeEnd() == -1) {
			tv.setText(" --:-- ");
		} else {
			tv.setText(String.format(Locale.getDefault(), " %tR ", mTW.getCalEnd()));
		}
	}

	public void calcTimesInRange(long overTimes[], Calendar calStart, Calendar calEnd, 
			LongRef workedTime, LongRef overTime) {
		Prefs prefs = new Prefs(mContext);
        TimesDataSource db = new TimesDataSource(mContext);
	    db.open();
        List<Times> values = db.getTimeRangeTimes(calStart.getTimeInMillis(), calEnd.getTimeInMillis(), "ASC");
        db.close();
	    int cnt = values.size();
	    workedTime.value = 0;
	    overTime.value = 0;
	    long workedPerDay = 0; // worked time per day, to support multiple entries on the same day
	    for (int i = 0; i < 7; i++) overTimes[i] = 0;
	    for (int i = 0; i < cnt; i++) {
	    	Times ti = values.get(i);
	    	// do only calc worked time and not overtime, as we do not know jet, if we have more entries on the same day
    		workedPerDay += TimeUtil.getWorkedTime(mContext, ti.getTimeStart(), ti.getTimeEnd());
    		Times ti_next;
    		// do we have a next value?
	    	if (i + 1 < cnt) {
	    		// yes, use that
	    		ti_next = values.get(i + 1);
	    	} else {
	    		// no, set day to next day (which is != current day) to finish calculating this day (see if below)
	    		ti_next = new Times(0, ti.getTimeStart() + 24 * 60 * 60 * 1000, 0);
	    	}
	    	// do we have more values with same day? 
	    	if (ti.getCalStart().get(Calendar.DAY_OF_MONTH) != ti_next.getCalStart().get(Calendar.DAY_OF_MONTH)) {
	    		// no, finish calculating this day
	    		workedTime.value += workedPerDay;
	    	    long ovt = workedPerDay - prefs.getHoursInMillis();
	    	   	overTime.value += ovt;
	    	   	int dayOfWeek = ti.getCalStart().get(Calendar.DAY_OF_WEEK);
	    	   	overTimes[dayOfWeek - 1] += ovt;
	    		workedPerDay = 0;
	    	}
	    }
	}

	public void calcCalendarWeekRange(Calendar calStart, Calendar calEnd) {
		calStart.set(Calendar.HOUR_OF_DAY, 0);
		calStart.set(Calendar.MINUTE, 0);
		
		// calculate first day of week
		int dayOfWeek = calStart.get(Calendar.DAY_OF_WEEK);
        calStart.add(Calendar.DAY_OF_MONTH, 1 - dayOfWeek);
        
        // calculate last day of week
        calEnd.setTimeInMillis(calStart.getTimeInMillis());
        calEnd.add(Calendar.DAY_OF_MONTH, 7);
  	}

	public void calcCalendarMonthRange(Calendar calStart, Calendar calEnd) {
		// calculate first day of month
		calStart.set(Calendar.HOUR_OF_DAY, 0);
		calStart.set(Calendar.MINUTE, 0);
		calStart.set(Calendar.DAY_OF_MONTH, 1);

        // calculate last day of month
        calEnd.setTimeInMillis(calStart.getTimeInMillis());
        calEnd.set(Calendar.HOUR_OF_DAY, 23);
        calEnd.set(Calendar.MINUTE, 59);
        int maxDayOfMonth = calEnd.getActualMaximum(Calendar.DAY_OF_MONTH);
        calEnd.set(Calendar.DAY_OF_MONTH, maxDayOfMonth);
  	}

	@SuppressLint("SetTextI18n")
	public void updateStatisticsView() {
		View view = getView();
		// only update the view if all global variables are valid ...
		if ((view == null) || (mTW == null)) {
			Log.d(LOG_TAG, "view or mTW is null. Not executing updateStatisticsView() ...");
			return;
		}
		Calendar calStart;
		Calendar calEnd = Calendar.getInstance();
	    LongRef workedTime = new LongRef(0);
	    LongRef overTime = new LongRef(0);
		long overTimes[] = new long[7];
		int weekTableIds[] = {0, R.id.week_table_mo_val, R.id.week_table_tu_val,
				R.id.week_table_we_val, R.id.week_table_th_val, R.id.week_table_fr_val, 0};
		TextView tv;
		
		// weekly statistics
		calStart = (Calendar) mTW.getStatisticsDate().clone();
		calcCalendarWeekRange(calStart, calEnd);
        calcTimesInRange(overTimes, calStart, calEnd, workedTime, overTime);
        Log.d(LOG_TAG, String.format(Locale.getDefault(),
				"weekly statistics: %1$td.%1$tm.%1$tY %1$tR - %2$td.%2$tm.%2$tY %2$tR", calStart, calEnd));

		calStart.add(Calendar.DAY_OF_MONTH, 1); // add one day, to get CW from Monday instead of Sunday
		tv = view.findViewById(R.id.stats_weekly_text);
        tv.setText(String.format(Locale.getDefault(), getResources().getString(R.string.stats_weekly_text),
        		calStart.get(Calendar.WEEK_OF_YEAR)));
		tv = view.findViewById(R.id.stats_weekly_worked_val);
        tv.setText(TimeUtil.formatTimeString(workedTime.value));
		tv = view.findViewById(R.id.stats_weekly_overtime_val);
        tv.setText("(" + TimeUtil.formatTimeString(overTime.value) + ")");

        // fill week table
	    for (int i = 1; i < 6; i++) {
			if (weekTableIds[i] != 0) {
				tv = view.findViewById(weekTableIds[i]);
				tv.setText(TimeUtil.formatTimeString(overTimes[i]));
				if (overTimes[i] < 0) {
					tv.setTextColor(ContextCompat.getColor(mContext, R.color.sysRed));
				} else {
					tv.setTextColor(ContextCompat.getColor(mContext, R.color.sysBlack));
				}
			}
	    }

        // monthly statistics
		calStart = (Calendar) mTW.getStatisticsDate().clone();
		calcCalendarMonthRange(calStart, calEnd);
        calcTimesInRange(overTimes, calStart, calEnd, workedTime, overTime);
		Log.d(LOG_TAG, String.format(Locale.getDefault(),
				"monthly statistics: %1$td.%1$tm.%1$tY %1$tR - %2$td.%2$tm.%2$tY %2$tR", calStart, calEnd));

		tv = view.findViewById(R.id.stats_monthly_text);
        tv.setText(String.format(Locale.getDefault(), getResources().getString(R.string.stats_monthly_text),
        		android.text.format.DateFormat.format("MMM", calStart.getTimeInMillis())));
		tv = view.findViewById(R.id.stats_monthly_worked_val);
        tv.setText(TimeUtil.formatTimeString(workedTime.value));
		tv = view.findViewById(R.id.stats_monthly_overtime_val);
        tv.setText("(" + TimeUtil.formatTimeString(overTime.value) + ")");
	}
	
	@Override
	public void onResume() {
		super.onResume();
    	updateTimesView();
    	updateStatisticsView();
	    mHandlerUpdateTimes.removeCallbacks(mUpdateTimesTask);
		// do another update in 100 millis, because at first call all pos. and widths are 0 ...
	    mHandlerUpdateTimes.postDelayed(mUpdateTimesTask, 100);
	}

	@Override
	public void onPause() {
		super.onPause();
		// remove all pending posts
		mHandlerUpdateTimes.removeCallbacks(mUpdateTimesTask);
	}

	private Runnable mUpdateTimesTask = new Runnable() {
	    public void run() {
	    	updateTimesView();
	    	// set next update post delayed
	        mHandlerUpdateTimes.postDelayed(mUpdateTimesTask, HANDLER_UPDATE_TIMES_DELAY);
	    }
	};
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		//  clean up stored references to avoid leaking
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mContext.unregisterReceiver(receiverUpdateView);
		//  clean up stored references to avoid leaking
		mContext = null;
		mTW = null;
	}

}
