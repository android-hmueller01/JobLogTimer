/*
 * @file ViewSectionFragment.java
 * @author Holger Mueller
 *
 * based on http://www.vogella.com/tutorials/AndroidSQLite/article.html#databasetutorial
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.euhm.jlt.dao.Times;
import de.euhm.jlt.dao.TimesDataSource;
import de.euhm.jlt.dao.TimesWork;
import de.euhm.jlt.dialogs.EditTimesFragment;
import de.euhm.jlt.dialogs.EditTimesFragment.OnEditTimesFragmentListener;
import de.euhm.jlt.listadapter.ViewTimesListAdapter;
import de.euhm.jlt.preferences.Prefs;
import de.euhm.jlt.utils.Constants;
import de.euhm.jlt.utils.TimeUtil;

/**
 * Fragment for viewing the times database
 *
 * @author hmueller
 */
public class ViewSectionFragment extends ListFragment {

	private final String LOG_TAG = ViewSectionFragment.class.getSimpleName();
	private TimesDataSource mDatasource; // gets used outside the normal initialization
	private Context mContext; // gets initialized in onAttach()
	private TimesWork mTW; // gets initialized in onCreate()
	private ListView mListView;
	private boolean mViewCreated = false; // set to true after view is created
	private CustomViewPager mViewPager;

	/**
	 * A {@link BroadcastReceiver} to update the view in this fragment.<br>
	 * Register in onCreate() and unregister in onDestroy()!
	 */
	private final BroadcastReceiver receiverUpdateView = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(LOG_TAG, "Received BroadcastReceiver " + Constants.RECEIVER_UPDATE_VIEW);
			// update ListAdapter and info line at top
			ViewTimesListAdapter adapter = (ViewTimesListAdapter) getListAdapter();
			assert adapter != null;
			adapter.refill(getListTimes());
			updateInfoLine();
		}
	};

	public ViewSectionFragment() {
		// save this in static MainActivity variable for later use, because
		// class gets reinstated outside MainActivity e.g. by rotating the device
		MainActivity.mViewSectionFragment = this;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		mContext = context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		// get current Work Times DAO
		mTW = new TimesWork(mContext);

		// get Times database
		mDatasource = new TimesDataSource(mContext);
		mDatasource.open();

		// register the update view receiver to update view from service
		LocalBroadcastManager.getInstance(mContext)
				.registerReceiver(receiverUpdateView, new IntentFilter(Constants.RECEIVER_UPDATE_VIEW));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_section_view,
				container, false);

		List<Times> listTimes = getListTimes();

		// use the ViewTimesListAdapter to show the elements in a ListView
		//ArrayAdapter<Times> adapter = new ArrayAdapter<Times>(this, android.R.layout.simple_list_item_1, listTimes);
		ViewTimesListAdapter adapter = new ViewTimesListAdapter(mContext, R.layout.list_item_view_times, listTimes);
		setListAdapter(adapter);

		mViewCreated = true;
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		updateInfoLine();

		// get the viewpager of the tabLayout toolbar
		mViewPager = requireActivity().findViewById(R.id.pager);

		// dynamically hide and show ActionBar
		mListView = getListView();
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			int lastVisibleItem = 0; // remember last firstVisibleItem in onScroll()

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
				assert actionBar != null;
				if (mViewPager != null &&
						view.getId() == mListView.getId() &&
						mViewPager.getCurrentItem() == 1 &&
						visibleItemCount * 2 < totalItemCount) {
					// hide ActionBar only, if we have at least double entries than visible entries
					if (firstVisibleItem > lastVisibleItem + 1) {
						// list is scrolled down (finger moved up)
						actionBar.hide();
						lastVisibleItem = firstVisibleItem;
					} else if (firstVisibleItem < lastVisibleItem - 1) {
						// list is scrolled up (finger moved down)
						actionBar.show();
						lastVisibleItem = firstVisibleItem;
					}
				}
			}
		});
		// handle item clicks (edit item)
		mListView.setOnItemClickListener((parent, view1, pos, id) -> {
			Log.d(LOG_TAG, "onListItemClick, pos=" + pos);

			EditTimesFragment frag = new EditTimesFragment();
			frag.setTimes((Times) mListView.getItemAtPosition(pos));
			// Display the edit fragment as the main content.
			//.replace(android.R.id.content, new EditTimesFragment())
			//.addToBackStack(null)
			getFragmentManager().beginTransaction()
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.add(0, frag)
					.commit();
		});
		// handle long press clicks (delete item)
		// Do NOT set android:longClickable="true" in XML!
		// @param parent The AbsListView where the click happened
		// @param view The view within the AbsListView that was clicked
		// @param pos The position of the view in the list
		// @param id The row id of the item that was clicked
		// @return true if the callback consumed the long click, false otherwise
		mListView.setOnItemLongClickListener((parent, view2, pos, id) -> {
			Log.d(LOG_TAG, "onItemLongClick, pos=" + pos);

			Times times = (Times) mListView.getItemAtPosition(pos);
			((OnEditTimesFragmentListener) requireActivity()).
					onFinishEditTimesFragment(Constants.BUTTON_DELETE, times);
			return true; // handle as long click, otherwise this will be handled as normal click
		});
	}

	/**
	 * Check if view is already created.
	 *
	 * @return Returns true if created.
	 * @noinspection unused
	 */
	public boolean isViewCreated() {
		return mViewCreated;
	}

	/**
	 * Get list of times depending on filter
	 *
	 * @return list of times
	 */
	private List<Times> getListTimes() {
		List<Times> listTimes;
		// check if database is open, might be called after onPause() by BroadcastReceiver ...
		boolean closeDatabase = false;
		if (!mDatasource.isOpen()) {
			mDatasource.open();
			closeDatabase = true;
		}

		// build list
		if (mTW.getFilterMonth() == 0 || mTW.getFilterYear() == 0) {
			// get all data
			listTimes = mDatasource.getAllTimes();
		} else {
			// get filtered range
			Calendar cal = TimeUtil.getFilterCal(mTW.getFilterMonth(), mTW.getFilterYear());
			long timeStart = cal.getTimeInMillis();
			// get end of month
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			// really set the end of the day, otherwise the last day will not be shown on the last day
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			long timeEnd = cal.getTimeInMillis();
			listTimes = mDatasource.getTimeRangeTimes(timeStart, timeEnd);
		}

		if (closeDatabase) {
			mDatasource.close();
		}

		return listTimes;
	}

	/**
	 * Update summary info line
	 */
	private void updateInfoLine() {
		// get views of times info line
		TextView viewInfoLineLeft = requireView().findViewById(R.id.view_times_info_line_left);
		TextView viewInfoLineMiddle = requireView().findViewById(R.id.view_times_info_line_middle);
		TextView viewInfoLineRight = requireView().findViewById(R.id.view_times_info_line_right);

		// set default values (if we have leave before setting at the end)
		String strLeft = String.format(Locale.getDefault(),
				getResources().getString(R.string.view_times_info_line_left), 0);
		String strMiddle = "";
		String strFilterActive = "";
		if (mTW.getFilterMonth() != 0 && mTW.getFilterYear() != 0) {
			// filter is active, indicate that
			strFilterActive = " " + getResources().getString(R.string.view_times_info_line_filter_active);
			Calendar cal = TimeUtil.getFilterCal(mTW.getFilterMonth(), mTW.getFilterYear());
			strMiddle = String.format(Locale.getDefault(), "%s %04d",
					android.text.format.DateFormat.format("MMM", cal.getTimeInMillis()),
					mTW.getFilterYear());
		}
		viewInfoLineLeft.setText(String.format("%s%s", strLeft, strFilterActive));
		viewInfoLineMiddle.setText(strMiddle);
		viewInfoLineRight.setText("");

		// get filtered list (if available) and calculate times
		ListAdapter la = getListAdapter();
		if (la == null) return;
		int cnt = la.getCount();
		if (cnt == 0) return;
		long workedTime = 0;
		long overTime = 0;
		long workedPerDay = 0; // worked time per day, to support multiple entries on the same day
		Prefs prefs = new Prefs(mContext);
		for (int i = 0; i < cnt; i++) {
			Times ti = (Times) la.getItem(i);
			// do only calc worked time and not overtime, as we do not know jet, if we have more entries on the same day
			workedPerDay += TimeUtil.getWorkedTime(mContext, ti.timeStart, ti.timeEnd, ti.homeOffice);
			Times ti_next;
			// do we have a next value?
			if (i + 1 < cnt) {
				// yes, use that
				ti_next = (Times) la.getItem(i + 1);
			} else {
				// no, set day to next day (which is != current day) to finish calculating this day (see if below)
				ti_next = new Times(0, ti.timeStart + 24 * 60 * 60 * 1000, 0, ti.homeOffice);
			}
			// do we have more values with same day?
			if (ti.getCalStart().get(Calendar.DAY_OF_MONTH) != ti_next.getCalStart().get(Calendar.DAY_OF_MONTH)) {
				// no, finish calculating this day
				workedTime += workedPerDay;
				overTime += workedPerDay - prefs.getHoursInMillis();
				workedPerDay = 0;
			}
		}

		// update info line with calculated values
		viewInfoLineLeft.setText(String.format(Locale.getDefault(),
				getResources().getString(R.string.view_times_info_line_left) + strFilterActive,
				cnt));
		viewInfoLineMiddle.setText(String.format(Locale.getDefault(),
				getResources().getString(R.string.view_times_info_line_middle),
				((Times) la.getItem(cnt - 1)).getDateString(),
				((Times) la.getItem(0)).getDateString()));
		viewInfoLineRight.setText(String.format(Locale.getDefault(),
				getResources().getString(R.string.view_times_info_line_right),
				TimeUtil.formatTimeString(workedTime),
				TimeUtil.formatTimeString(overTime)));
	}

	public void addTimesItem(Times times) {
		if (times == null) {
			Toast.makeText(mContext, "addTimesItem: times is null! Exit!", Toast.LENGTH_LONG).show();
			return;
		}

		times = mDatasource.createTimes(times);
		ViewTimesListAdapter adapter = (ViewTimesListAdapter) getListAdapter();
		assert adapter != null;
		adapter.setNotifyOnChange(false); // do not notify changes yet
		adapter.add(times);
		adapter.sort();
		adapter.notifyDataSetChanged(); // now update the changed data
		updateInfoLine();
	}

	public void updateTimesItem(Times times) {
		if (times == null) {
			Toast.makeText(mContext, "updateTimesItem: times is null! Exit!", Toast.LENGTH_LONG).show();
			return;
		}

		int rows = mDatasource.updateTimes(times);
		if (rows != 1) {
			Toast.makeText(mContext, "Database update failed. rows should be 1 but is " + rows, Toast.LENGTH_LONG).show();
		}
		// only needed,  if list was recreated in the meantime (e.g. by rotating the device)
		ViewTimesListAdapter adapter = (ViewTimesListAdapter) getListAdapter();
		assert adapter != null;
		adapter.setNotifyOnChange(false); // do not notify changes yet
		adapter.update(times);
		adapter.sort();
		adapter.notifyDataSetChanged(); // now update the changed data
		updateInfoLine();
	}

	public void deleteTimesItem(Times times) {
		if (times == null) {
			Toast.makeText(mContext, "deleteTimesItem: times is null! Exit!", Toast.LENGTH_LONG).show();
			return;
		}

		int rows = mDatasource.deleteTimes(times);
		if (rows != 1) {
			Toast.makeText(mContext, "Database delete failed. rows should be 1 but is " + rows, Toast.LENGTH_LONG).show();
		}

		ViewTimesListAdapter adapter = (ViewTimesListAdapter) getListAdapter();
		assert adapter != null;
		adapter.remove(times); // remove item from adapter list
		adapter.notifyDataSetChanged(); // now update the changed data
		updateInfoLine();
	}

	public void updateFilter(int month, int year) {
		mTW.setFilterMonth(month);
		mTW.setFilterYear(year);

		// update ListAdapter
		ViewTimesListAdapter adapter = (ViewTimesListAdapter) getListAdapter();
		assert adapter != null;
		adapter.refill(getListTimes());
		updateInfoLine();
	}

	/**
	 * Backup internal database to local storage.<br />
	 * Previous backup will be overwritten without warning!
	 *
	 * @param dbPath Path to export to.
	 */
	public void exportDatabase(String dbPath) {
		try {
			boolean result;
			result = mDatasource.getDbHelper().exportDatabase(dbPath);
			if (result) {
				Toast.makeText(mContext, "Backed up database to " + dbPath, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(mContext, "Failed to backup database.", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Toast.makeText(mContext, "Failed to backup database.", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Restore internal database from local storage (backup).<br />
	 * <i><b>WARNING:</b> This will delete the current database and replace it
	 * with the backup!</i>
	 *
	 * @param dbPath Path to import from
	 */
	public void importDatabase(String dbPath) {
		try {
			boolean result;
			mDatasource.close(); // close database before import
			result = mDatasource.getDbHelper().importDatabase(dbPath);
			mDatasource.open(); // reopen database after import
			if (result) {
				// update views that changes take place
				mContext.sendBroadcast(new Intent(Constants.RECEIVER_UPDATE_VIEW));

				Toast.makeText(mContext, "Imported database " + dbPath, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(mContext, "Failed to import database " + dbPath, Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Toast.makeText(mContext, "Failed to import database " + dbPath,
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mDatasource.open();
	}

	@Override
	public void onPause() {
		super.onPause();
		mDatasource.close();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mViewCreated = false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mContext.unregisterReceiver(receiverUpdateView);
		//  clean up stored references to avoid leaking
		mTW = null;
		mListView = null;
		mDatasource = null;
		mViewPager = null;
		mContext = null;
	}
}