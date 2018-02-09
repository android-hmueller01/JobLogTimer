/**
 * $Id: MainActivity.java 196 2017-01-16 16:58:20Z hmueller $
 * 
 * Main activity of JobLog
 * based on Google EffectiveNavigation example
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import de.euhm.jlt.dao.Times;
import de.euhm.jlt.dao.TimesWork;
import de.euhm.jlt.database.JobLogContract;
import de.euhm.jlt.dialogs.ConfirmDialogFragment;
import de.euhm.jlt.dialogs.DatePickerFragment;
import de.euhm.jlt.dialogs.EditTimesFragment;
import de.euhm.jlt.dialogs.FilterFragment;
import de.euhm.jlt.dialogs.TimePickerFragment;
import de.euhm.jlt.services.EndWorkService;
import de.euhm.jlt.services.StartWorkService;
import de.euhm.jlt.utils.AlarmUtils;
import de.euhm.jlt.utils.Constants;
import de.euhm.jlt.utils.TimeUtil;

/**
 * Main activity of JobLog
 * @author hmueller
 */
public class MainActivity extends AppCompatActivity implements
	NavigationView.OnNavigationItemSelectedListener,
	EditTimesFragment.OnEditTimesFragmentListener,
	ConfirmDialogFragment.YesNoListener,
	DatePickerFragment.OnDatePickerFragmentListener,
	TimePickerFragment.OnTimePickerFragmentListener,
	FilterFragment.OnFilterFragmentListener,
	ActivityCompat.OnRequestPermissionsResultCallback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private Times mTimes; // temp. Times for different dialogs
	private TimesWork mTW; // global TimesWork data (static internal data, so same access from all fragments)
    private Menu mMainMenu; // saved MainMenu for later use in onKeyUp() and onTabSelected()
	public static MainSectionFragment mMainSectionFragment = new MainSectionFragment();
    public static ViewSectionFragment mViewSectionFragment = new ViewSectionFragment();
    private String mBackupDbPath;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
	 * primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	AppSectionsPagerAdapter mAppSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will display the primary sections of the app, one at a
	 * time.
	 */
	CustomViewPager mViewPager;

	/**
	 * The {@link android.support.v4.widget.DrawerLayout} that will provide full 
	 * material design drawer navigation back to Android v4.
	 */
	DrawerLayout mDrawerLayout;
	int mDrawerState; // state of drawer (s. e.g. DrawerLayout.STATE_SETTLING)

	/**
	 * The {@link android.support.design.widget.FloatingActionButton} that will
	 * provide full material design Floating Action Button back to Android v7.
	 */
	FloatingActionButton mFab;

	/**
	 * A {@link BroadcastReceiver} to restart the activity.<br>
	 * Register in onCreate() and unregister in onDestroy()!
	 */
	private final BroadcastReceiver receiverRecreate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
			Log.v(LOG_TAG, "Received BroadcastReceiver " + Constants.RECEIVER_RECREATE);
        	if (Build.VERSION.SDK_INT >= 11) {
        	    recreate();
        	} else {
        	    Intent mainIntent = getIntent();
        	    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        	    finish();
        	    overridePendingTransition(0, 0);
        	    startActivity(mainIntent);
        	    overridePendingTransition(0, 0);
        	}
        }
	};

	/**
	 * A {@link BroadcastReceiver} to update the view in this activity.<br>
	 * Register in onCreate() and unregister in onDestroy()!
	 */
	private final BroadcastReceiver receiverUpdateView = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
			Log.v(LOG_TAG, "Received BroadcastReceiver " + Constants.RECEIVER_UPDATE_VIEW);
            supportInvalidateOptionsMenu();
        }
	};


    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Context context = getApplicationContext();
		setContentView(R.layout.activity_main);

		// register the recreate receiver to restart activity from service
	    registerReceiver(receiverRecreate, new IntentFilter(Constants.RECEIVER_RECREATE));
		// register the update view receiver to update view from service
	    registerReceiver(receiverUpdateView, new IntentFilter(Constants.RECEIVER_UPDATE_VIEW));

	    // set path and name of backup database used by db export and import
	    mBackupDbPath = 
	    		Environment.getExternalStorageDirectory().getAbsolutePath() +
				File.separatorChar + getResources().getString(R.string.app_name) +
				File.separatorChar + JobLogContract.DATABASE_NAME;

		// do all the initialization stuff only once, when app gets really started
		if (savedInstanceState == null) {
			// initialize preferences with default settings
			PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		} else {
			if (mTimes == null) mTimes = new Times(0, 0, 0);
			mTimes.loadInstanceState(savedInstanceState);
			if (mTimes.getId() == 0 && mTimes.getTimeStart() == 0 && mTimes.getTimeEnd() == 0) {
				// no data loaded, remove mTimes
				mTimes = null;
			}
		}
		
		// Construct TimesWork DAO with persistent data
		mTW = new TimesWork(context);
		//mTW.timeEnd(-1); // only for debugging, resetting End time

		// Setup FloatingActionButton from android.support.design.widget.FloatingActionButton for API < 21.
		mFab = (FloatingActionButton) findViewById(R.id.fab);
		// handle the FloatingActionButton click event
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
    			// prepare to add a new SQL row
    			long currentTime = TimeUtil.getCurrentTimeInMillis();
    			Times times = new Times(-1, currentTime, currentTime);
    			
    			// prepare to edit entry
    	        EditTimesFragment frag = new EditTimesFragment();
    	        frag.setTimes(times);
    	        // Display the edit fragment as the main content.
    	        getSupportFragmentManager().beginTransaction()
    				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
    				.add(0, frag)
    				.commit();
            }
        });

        // Setup the action bar from android.support.v7.app.ActionBar for API < 21.
        Toolbar supportToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(supportToolbar);
		final ActionBar actionBar = getSupportActionBar();

		// Specify that the Home/Up button should not be enabled, since there is no hierarchical parent.
		actionBar.setHomeButtonEnabled(false);

        // Setup the ViewPager.
		mViewPager = (CustomViewPager) findViewById(R.id.pager);
		// Create the adapter that will return a fragment for each of the primary sections of the app.
		mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager(), context);
		mViewPager.setAdapter(mAppSectionsPagerAdapter);

        // Setup the TabLayout and connect it to the ViewPager.
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        // setupWithViewPager() does not work very well, so we do all the things ourself (see below)
        //tabLayout.setupWithViewPager(mViewPager);
		mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
        	@Override
        	public void onTabSelected(TabLayout.Tab tab) {
        		// When the given tab is selected, switch to the corresponding page in the ViewPager.
                int position = tab.getPosition();
        		mViewPager.setCurrentItem(position);
        		if (position == 1) {
        			// show FloatingActionButton
        			mFab.show();
        			// show special menu entries to be more responsive
        			// Needs to be set in onPrepareOptionsMenu() as well!
        			if (mMainMenu != null) {
        				mMainMenu.findItem(R.id.action_filter).setVisible(true);
        			}
        		} else {
        			// tab position != 1
        			// hide FloatingActionButton
        			mFab.hide();
        			// hide special menu entries to be more responsive
        			if (mMainMenu != null) {
        				mMainMenu.findItem(R.id.action_filter).setVisible(false);
        			}
        			// ensure, that ActionBar is shown
        	        getSupportActionBar().show();
        		}
        	}

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by the adapter.
			// Also specify this Activity object, which implements the TabListener interface, as the
			// listener for when this tab is selected.
			Tab tab = tabLayout.newTab();
			tab.setCustomView(R.layout.custom_tab_title);
			View tab_view = tab.getCustomView();
			TextView tab_title = (TextView) tab_view.findViewById(R.id.title);
			ImageView img = (ImageView) tab_view.findViewById(R.id.icon);
			tab_title.setText(mAppSectionsPagerAdapter.getPageTitle(i));
			int icon = mAppSectionsPagerAdapter.getPageIcon(i);
			if (icon != 0)
				img.setImageResource(mAppSectionsPagerAdapter.getPageIcon(i));
			tabLayout.addTab(tab);
		}


		// setup the navigation drawer view from android.support.v4.widget.DrawerLayout for API < 21.
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
		// Necessary for automatically animated navigation drawer upon open and close.
		// The two strings are not displayed to the user.
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, 
				supportToolbar, R.string.drawer_open, R.string.drawer_close) {
		    @Override
		    public void onDrawerStateChanged(int newState) {
	        	mDrawerState = newState;
		    }
		};
		mDrawerLayout.addDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_drawer);
		navigationView.setNavigationItemSelectedListener(this);
	}


    /* ******************************************************************
	 * FragmentPagerAdapter to handle tabs
	 * ****************************************************************** */

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
	 * sections of the app.
	 */
	public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
		
		private Context mContext;

		public AppSectionsPagerAdapter(FragmentManager fm, Context context) {
			super(fm);
			mContext = context;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0: // Main tab
				return mMainSectionFragment;
			case 1: // View tab
				return mViewSectionFragment;
			case 2: // Settings tab
				return new SettingsSectionFragment();
			default:
				// Unknown section number. Throw exception.
				throw new RuntimeException("Section item " + position + "unknown");
			}
		}
		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0: // Main tab
				return mContext.getResources().getString(R.string.tab_main);
			case 1: // View tab
				return mContext.getResources().getString(R.string.tab_view);
			case 2: // Settings tab
				return mContext.getResources().getString(R.string.tab_settings);
			}
			return "";
		}
		
		public int getPageIcon(int position) {
			switch (position) {
			case 0: // Main tab
				return R.drawable.ic_tab_start;
			case 1: // View tab
				return R.drawable.ic_tab_view;
			case 2: // Settings tab
				return R.drawable.ic_tab_settings;
			}
			return 0;
		}
	}


	/* ******************************************************************
	 * Options menu stuff (onOptionsItemSelected)
	 * ****************************************************************** */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        mMainMenu = menu; // store the menu in a local variable

        return super.onCreateOptionsMenu(menu);
    }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Prepare the Screen's standard options menu to be displayed. 
		// This is called right before the menu is shown, every time it is shown. You can use this
		// method to efficiently enable/disable items or otherwise dynamically modify the contents.

		// show start/end action button
		if (mTW.getWorkStarted()) {
			menu.findItem(R.id.action_start).setVisible(false);
			menu.findItem(R.id.action_end).setVisible(true);
		} else {
			menu.findItem(R.id.action_start).setVisible(true);
			menu.findItem(R.id.action_end).setVisible(false);
		}
		// show special menu entries only on view tab
		if (mViewPager.getCurrentItem() == 1) {
			menu.findItem(R.id.action_filter).setVisible(true);
		} else {
			menu.findItem(R.id.action_filter).setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		// Handle presses on the action bar items
		switch (id) {
		case R.id.action_start:
			startService(new Intent(this, StartWorkService.class));
			return true;
		case R.id.action_end:
			startService(new Intent(this, EndWorkService.class));
			return true;
		case R.id.action_filter:
			// prepare to edit filter
	        FilterFragment filterFrag = new FilterFragment();
	        filterFrag.setPickerMonth(mTW.getFilterMonth());
	        filterFrag.setPickerYear(mTW.getFilterYear());
	        // Display the edit fragment as the main content.
	        getSupportFragmentManager().beginTransaction()
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.add(0, filterFrag)
				.commit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

/* removed, as with v0.10 we have no "more" menu any more
 * left commented in case we need it again
	@Override
	public boolean onKeyUp(int keycode, KeyEvent event) {
	    switch(keycode) {
	    case KeyEvent.KEYCODE_MENU:
	    	if (mMainMenu != null && 
	    		mMainMenu.findItem(R.id.action_more) != null) {
	    		mMainMenu.performIdentifierAction(R.id.action_more, 0);
	    		return true;
	    	}
	    	break;
	    }
	    return super.onKeyUp(keycode, event);
	}
*/

	/* ******************************************************************
	 * Navigation drawer menu stuff (onNavigationItemSelected)
	 * ****************************************************************** */

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public boolean onNavigationItemSelected(MenuItem item) { 
        // Close drawer on item click
        mDrawerLayout.closeDrawers();
        
        // Get item ID to determine what to do on user click
        int itemId = item.getItemId();
        switch (itemId) {
		case R.id.nav_item_add_times:
			// prepare to add a new SQL row
			long currentTime = TimeUtil.getCurrentTimeInMillis();
			Times times = new Times(-1, currentTime, currentTime);
			
			// prepare to edit entry
	        EditTimesFragment frag = new EditTimesFragment();
	        frag.setTimes(times);
	        // Display the edit fragment as the main content.
	        getSupportFragmentManager().beginTransaction()
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.add(0, frag)
				.commit();
	        break;
        case R.id.nav_item_backup:
			// backup internal database to external file
			// check if we have permission to write external storage
			if (ContextCompat.checkSelfPermission(this,
					Manifest.permission.WRITE_EXTERNAL_STORAGE)	!= 
					PackageManager.PERMISSION_GRANTED) {
				// permission is not granted, ask for permission and wait for
				// callback method onRequestPermissionsResult gets the result of the request.
				// PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE is an app-defined int constant.
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						Constants.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
			} else {
				// permission is ok, do the backup right now
				mViewSectionFragment.exportDatabase(mBackupDbPath);
			}
	        break;
        case R.id.nav_item_restore:
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			String message = getResources().getText(R.string.confirm_restore).toString();
			message = String.format(Locale.getDefault(), message, mBackupDbPath);
			
			// set dialog message
			//.setTitle("Title")
			alertDialogBuilder
				.setMessage(message)
				.setCancelable(true)
				.setPositiveButton(android.R.string.yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// if "Yes" this button is clicked
							// restore backup database to internal database
							if (Build.VERSION.SDK_INT < 16) {
								// in API < 16 we are always allowed to read from external storage
								// do the restore right now
								mViewSectionFragment.importDatabase(mBackupDbPath);
							} else {
								// check if we have permission to read from external storage
								if (ContextCompat.checkSelfPermission(MainActivity.this,
										Manifest.permission.READ_EXTERNAL_STORAGE)	!= 
										PackageManager.PERMISSION_GRANTED) {
									// permission is not granted, ask for permission and wait for
									// callback method onRequestPermissionsResult gets the result of the request.
									// PERMISSION_REQUEST_READ_EXTERNAL_STORAGE is an app-defined int constant.
									ActivityCompat.requestPermissions(MainActivity.this,
										new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
										Constants.PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
								} else {
									// permission is ok, do the restore right now
									mViewSectionFragment.importDatabase(mBackupDbPath);
								}
							}
						}
					})
				.setNegativeButton(android.R.string.no,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// just close the dialog box and do nothing
							dialog.cancel();
						}
					});

			// create alert dialog and show it
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
        	break;
        case R.id.nav_item_about:
        	// Display the about fragment as the main content.
        	getSupportFragmentManager().beginTransaction()
        		.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        		.add(0, new AboutFragment())
        		.commit();
        	break;
        }
        return true;
	}

	@Override
	public void onBackPressed() {
		// if drawer is open, close it instead of closing the app
	    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
	    	mDrawerLayout.closeDrawer(GravityCompat.START);
	    } else {
	        super.onBackPressed();
	    }
	}

	/* ******************************************************************
	 * All listener of class MainActivity
	 * ****************************************************************** */
	
	private boolean mIsPinching = false;

	/**
     * Called to process touch screen events. 
     * Need this instead of view.setOnTouchListener(...) because of ScrollView layout.
     * See OnSwipeTouchListener call in class MainSectionFragment
     */
    @Override
	public boolean dispatchTouchEvent(MotionEvent event) {
    	boolean result = false;
    	boolean retVal= false;

    	// detect pinching
		int pointerCount = event.getPointerCount();
		int action = event.getAction();
		if (pointerCount > 1)
			mIsPinching = true;
		if (pointerCount == 1 && action == MotionEvent.ACTION_UP)
			mIsPinching = false;
		//Log.d(LOG_TAG, String.format("pointerCount=%d, mIsPinching=%b, action=%d", pointerCount, mIsPinching, action));

		if (mMainSectionFragment != null &&
				mViewPager.getCurrentItem() == 0) { // only in first tab (START)
			OnSwipeTouchListener onSwipeTouchListener = mMainSectionFragment.getOnSwipeTouchListener();
			// decide if we should swipe the view
			if (!mIsPinching && // do not if pinching is active
					mDrawerState == DrawerLayout.STATE_IDLE && // only if drawer is not moving
					!mDrawerLayout.isDrawerOpen(GravityCompat.START)) // only if drawer is closed
			{
				result = onSwipeTouchListener.onTouch(onSwipeTouchListener.getView(), event);
			} else if (mDrawerState == DrawerLayout.STATE_DRAGGING) {
				// terminate swipe if drawer opens by faking an action up event ...
				event.setAction(MotionEvent.ACTION_UP);
				onSwipeTouchListener.onTouch(onSwipeTouchListener.getView(), event);
				event.setAction(action);
			}
		}
    	if (!result) {
    		result = super.dispatchTouchEvent(event);
    	}
		return retVal || result;
	}

	// EditTimes Listener
	@Override
	public void onFinishEditTimesFragment(int id, Times times) {
		mTimes = times;
		String selection = null;
		Context context = getApplicationContext();
		
		switch (id) {
		case Constants.BUTTON_DELETE:
			// The user wants to deleted the date/time. Asked for confirmation.
			// Do not use an AlertDialog.Builder directly, because it doesn't get recreated
			// if app gets restarted (after pressing home or rotating)
			ConfirmDialogFragment confirmDialog = new ConfirmDialogFragment();
			confirmDialog.setMessage(getResources().getText(R.string.button_delete) + " " + times.getDateString());
			confirmDialog.show(getSupportFragmentManager(), "ConfirmDialogFragment");
			break;
		case Constants.BUTTON_OK:
			// The user selected a new date/time. Now update that.
			if (mViewSectionFragment == null || mMainSectionFragment == null) {
				// at this point mViewSectionFragment should never be null!
				throw new RuntimeException("onFinishEditTimesFragment: mViewSectionFragment is null!"); 
			}
			if (times.getId() == -1) {
				// new entry
				mViewSectionFragment.addTimesItem(times);
				selection = getResources().getString(R.string.database_add_positive_result);
			} else {
				// update existing entry
				mViewSectionFragment.updateTimesItem(times);
				selection = getResources().getString(R.string.database_update_positive_result);
			}
			// update statistics, it might change ...
			mMainSectionFragment.updateStatisticsView();
			mTimes = null; // mark as handled
			break;
		case Constants.BUTTON_CANCEL:
			// user canceled action (currently not used / called)
			mTimes = null; // mark as handled
			break;
		}

		if (selection != null) {
			Toast.makeText(context, selection, Toast.LENGTH_SHORT).show();
		}
	}
	
	// ConfirmDialogFragment Listener
	@Override
	public void onConfirmDialogYes() {
		// user acknowledged the deletion
		if (mViewSectionFragment == null || mMainSectionFragment == null) {
			// at this point mViewSectionFragment should never be null!
			throw new RuntimeException("onFinishEditTimesFragment->onConfirmDialogYes: mViewSectionFragment is null!"); 
		}
		mViewSectionFragment.deleteTimesItem(mTimes);
		// update statistics, it might change ...
		mMainSectionFragment.updateStatisticsView();
		Toast.makeText(this, R.string.database_delete_positive_result, Toast.LENGTH_SHORT).show();
		
		mTimes = null; // mark as handled
	}

	@Override
	public void onConfirmDialogNo() {
		// user declined the deletion
		mTimes = null; // mark as handled
	}
	
	// Filter Listener
	@Override
	public void onFinishFilterFragment(int id, int month, int year) {
		switch (id) {
		case Constants.BUTTON_OK:
		case Constants.BUTTON_CLEAR:
			// The user selected a new date/time. Now update that.
			if (mViewSectionFragment == null) {
				// at this point mViewSectionFragment should never be null!
				throw new RuntimeException("onFinishFilterFragment: mViewSectionFragment is null!"); 
			}
			// update filter
			mViewSectionFragment.updateFilter(month, year);
			break;
		case Constants.BUTTON_CANCEL:
			// user canceled action (currently not used / called)
			break;
		}
	}

	public void showDatePickerDialog(View v) {
		if (mTW.getWorkStarted()) {
			// do not set mTW.timeStart to current time, otherwise we can not cancel ...
			Calendar cal = TimeUtil.getCurrentTime();
			if (mTW.getTimeStart() != -1)  cal.setTimeInMillis(mTW.getTimeStart());
			DatePickerFragment datePicker = new DatePickerFragment();
			datePicker.set(cal, R.string.date_text);
			datePicker.show(getSupportFragmentManager(), "datePicker");
		} else {
			Toast.makeText(this, R.string.work_not_started, Toast.LENGTH_LONG).show();
		}
	}
	
	public void showTimePickerDialog_StartTime(View v) {
		if (mTW.getWorkStarted()) {
			// do not set mTW.timeStart to current time, otherwise we can not cancel ...
			Calendar cal = TimeUtil.getCurrentTime();
			if (mTW.getTimeStart() != -1)  cal.setTimeInMillis(mTW.getTimeStart());
			TimePickerFragment timePicker = new TimePickerFragment();
			timePicker.set(cal, R.string.start_time_set);
			timePicker.show(getSupportFragmentManager(), "timePicker");
		} else {
			Toast.makeText(this, R.string.work_not_started, Toast.LENGTH_LONG).show();
		}
	}

	public void showTimePickerDialog_EndTime(View v) {
		if (mTW.getWorkStarted()) {
			// do not set mTW.timeEnd to current time, otherwise we can not cancel ...
			Calendar cal = TimeUtil.getCurrentTime();
			if (mTW.getTimeEnd() != -1)  cal.setTimeInMillis(mTW.getTimeEnd());
			TimePickerFragment timePicker = new TimePickerFragment();
			timePicker.set(cal, R.string.end_time_set);
			timePicker.show(getSupportFragmentManager(), "timePicker");
		} else {
			Toast.makeText(this, R.string.work_not_started, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onFinishTimePickerFragment(Calendar cal, int titleId) {
		// set time depending on titleId
		switch (titleId) {
		case R.string.start_time_set:
			Calendar calTimeStart = TimeUtil.getCurrentTime();
			if (mTW.getTimeStart() != -1)  calTimeStart.setTimeInMillis(mTW.getTimeStart());
			calTimeStart.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
			calTimeStart.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
			mTW.setCalStart(calTimeStart);
			break;
		case R.string.end_time_set:
			Calendar calTimeEnd = TimeUtil.getCurrentTime();
			if (mTW.getTimeEnd() != -1)  calTimeEnd.setTimeInMillis(mTW.getTimeEnd());
			calTimeEnd.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
			calTimeEnd.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
			mTW.setCalEnd(calTimeEnd);
			break;
		}
		// update view and alarms ...
		mMainSectionFragment.updateTimesView();
		AlarmUtils.setAlarms(this, mTW);
	}
	
	@Override
	public void onFinishDatePickerFragment(Calendar cal, int titleId) {
		// set time depending on titleId
		switch (titleId) {
		case R.string.date_text:
			Calendar calTimeStart = TimeUtil.getCurrentTime();
			if (mTW.getTimeStart() != -1)  calTimeStart.setTimeInMillis(mTW.getTimeStart());
			calTimeStart.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
			mTW.setCalStart(calTimeStart);
			if (mTW.getTimeEnd() != -1) {
				Calendar calTimeEnd = mTW.getCalEnd();
				calTimeEnd.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
				mTW.setCalEnd(calTimeEnd);
			}
			break;
		}
		// update view and alarms ...
		mMainSectionFragment.updateTimesView();
		mMainSectionFragment.updateStatisticsView();
		AlarmUtils.setAlarms(this, mTW);
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, 
			String[] permissions, int[] grantResults) {
		// Handle the permissions request response.
		switch (requestCode) {
		case Constants.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE:
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        		// permission was granted, do the backup right now
        		mViewSectionFragment.exportDatabase(mBackupDbPath);
            }
            break;
		case Constants.PERMISSION_REQUEST_READ_EXTERNAL_STORAGE:
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        		// permission was granted, do the restore right now
        		mViewSectionFragment.importDatabase(mBackupDbPath);
            }
            break;
        }
    }


	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    // Always call the superclass so it can save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
		if (mTimes != null) {
			// Save the user's current selected time
			mTimes.saveInstanceState(savedInstanceState);
		}
	}
    
	@Override
	public void onResume() {
		super.onResume();
		// remember, that activity is resumed
		CustomApplication.activityResumed();
	}

	@Override
	public void onPause() {
		super.onPause();
		// remember, that activity is paused
		CustomApplication.activityPaused();
		// Store TimesWork data in persistent store
		mTW.saveTimesWork();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Unregister receiver, because after that activity is killed!
	    unregisterReceiver(receiverRecreate);
		unregisterReceiver(receiverUpdateView);
		//  clean up stored references to avoid leaking
		mAppSectionsPagerAdapter = null;
		mViewPager = null;
		mTimes = null;
		mTW = null;
		mMainMenu = null;
	}
}
