/**
 * @file MainActivity.kt
 * 
 * Main activity of JobLogTimer
 *
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentTransaction
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import de.euhm.jlt.dao.Times
import de.euhm.jlt.dao.TimesWork
import de.euhm.jlt.database.JobLogContract
import de.euhm.jlt.dialogs.ConfirmDialogFragment
import de.euhm.jlt.dialogs.ConfirmDialogFragment.YesNoListener
import de.euhm.jlt.dialogs.DatePickerFragment
import de.euhm.jlt.dialogs.DatePickerFragment.OnDatePickerFragmentListener
import de.euhm.jlt.dialogs.EditTimesFragment
import de.euhm.jlt.dialogs.EditTimesFragment.OnEditTimesFragmentListener
import de.euhm.jlt.dialogs.FilterFragment
import de.euhm.jlt.dialogs.FilterFragment.OnFilterFragmentListener
import de.euhm.jlt.dialogs.TimePickerFragment
import de.euhm.jlt.dialogs.TimePickerFragment.OnTimePickerFragmentListener
import de.euhm.jlt.receivers.AlarmReceiver
import de.euhm.jlt.receivers.StartStopReceiver
import de.euhm.jlt.utils.AlarmUtils
import de.euhm.jlt.utils.Constants
import de.euhm.jlt.utils.TimeUtil
import java.io.File
import java.util.Calendar
import java.util.Locale

/**
 * Main activity of JobLog
 * @author hmueller
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnEditTimesFragmentListener,
    YesNoListener, OnDatePickerFragmentListener, OnTimePickerFragmentListener, OnFilterFragmentListener,
    OnRequestPermissionsResultCallback {
    private var mTimes: Times? = null // temp. Times for different dialogs
    private lateinit var mTW: TimesWork // global TimesWork data (static internal data, so same access from all fragments)
    private var mMainMenu: Menu? = null // saved MainMenu for later use in onKeyUp() and onTabSelected()
    private lateinit var mBackupDbPath: String

    /**
     * The [AppSectionsPagerAdapter] that will provide fragments for each of the
     * primary sections of the app. We use a [FragmentPagerAdapter]
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a [FragmentStatePagerAdapter].
     */
    private lateinit var mAppSectionsPagerAdapter: AppSectionsPagerAdapter

    /**
     * The [ViewPager] that will display the primary sections of the app, one at a
     * time.
     */
    private lateinit var mViewPager: CustomViewPager

    /**
     * The [DrawerLayout] that will provide full
     * material design drawer navigation back to Android v4.
     */
    private lateinit var mDrawerLayout: DrawerLayout
    private var mDrawerState: Int = 0 // state of drawer (s. e.g. DrawerLayout.STATE_SETTLING)

    /**
     * The [FloatingActionButton] that will
     * provide full material design Floating Action Button back to Android v7.
     */
    private lateinit var mFab: FloatingActionButton

    /**
     * A [BroadcastReceiver] to restart the activity.<br></br>
     * Register in onCreate() and unregister in onDestroy()!
     */
    private val receiverRecreate: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.v(LOG_TAG, "Received BroadcastReceiver " + Constants.RECEIVER_RECREATE)
            recreate()
        }
    }

    /**
     * A [BroadcastReceiver] to update the view in this activity.<br></br>
     * Register in onCreate() and unregister in onDestroy()!
     */
    private val receiverUpdateView: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.v(LOG_TAG, "Received BroadcastReceiver " + Constants.RECEIVER_UPDATE_VIEW)
            supportInvalidateOptionsMenu()
        }
    }

    /**
     * A [BroadcastReceiver] to alarm the user.<br></br>
     * Register in onCreate() and unregister in onDestroy()!
     */
    private val alarmReceiver: BroadcastReceiver = AlarmReceiver()


    //@RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = applicationContext
        setContentView(R.layout.activity_main)

        // register the recreate receiver to restart activity from service
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiverRecreate, IntentFilter(Constants.RECEIVER_RECREATE))
        //registerReceiver(receiverRecreate, IntentFilter(Constants.RECEIVER_RECREATE), RECEIVER_NOT_EXPORTED)
        // register the update view receiver to update view from service
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiverUpdateView, IntentFilter(Constants.RECEIVER_UPDATE_VIEW))
        //registerReceiver(receiverUpdateView, IntentFilter(Constants.RECEIVER_UPDATE_VIEW), RECEIVER_NOT_EXPORTED)
        // register the alarm receiver
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(alarmReceiver, IntentFilter(Constants.RECEIVER_NORMAL_WORK_ALARM))
        //registerReceiver(alarmReceiver, IntentFilter(Constants.RECEIVER_NORMAL_WORK_ALARM), RECEIVER_NOT_EXPORTED)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(alarmReceiver, IntentFilter(Constants.RECEIVER_MAX_WORK_ALARM))
        //registerReceiver(alarmReceiver, IntentFilter(Constants.RECEIVER_MAX_WORK_ALARM), RECEIVER_NOT_EXPORTED)

        // set path and name of backup database used by db export and import
        // set path to Android/data/de.euhm.jlt/files to avoid permission requests
        // mBackupDbPath = getExternalFilesDir(null)!!.absolutePath + File.separatorChar + JobLogContract.DATABASE_NAME
        // path to external storage directory root with name of app
        mBackupDbPath = Environment.getExternalStorageDirectory().absolutePath +
                File.separatorChar + resources.getString(R.string.app_name) +
                File.separatorChar + JobLogContract.DATABASE_NAME

        // do all the initialization stuff only once, when app gets really started
        if (savedInstanceState == null) {
            // initialize preferences with default settings
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        } else {
            if (mTimes == null) mTimes = Times(0, 0, 0, false)
            mTimes!!.loadInstanceState(savedInstanceState)
            if (mTimes!!.id == 0L && mTimes!!.timeStart == 0L && mTimes!!.timeEnd == 0L) {
                // no data loaded, remove mTimes
                mTimes = null
            }
        }


        // Construct TimesWork DAO with persistent data
        mTW = TimesWork(context)

        //mTW.timeEnd(-1); // only for debugging, resetting End time

        // Setup FloatingActionButton from android.support.design.widget.FloatingActionButton for API < 21.
        mFab = findViewById(R.id.fab)
        // handle the FloatingActionButton click event
        mFab.setOnClickListener { // prepare to add a new SQL row
            val currentTime = TimeUtil.getCurrentTimeInMillis()
            val times = Times(-1, currentTime, currentTime, false)


            // prepare to edit entry
            val frag = EditTimesFragment()
            frag.setTimes(times)
            // Display the edit fragment as the main content.
            supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(0, frag).commit()
        }

        // Setup the action bar from android.support.v7.app.ActionBar for API < 21.
        val supportToolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(supportToolbar)

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical parent.
        supportActionBar?.setHomeButtonEnabled(false)

        // Setup the ViewPager.
        mViewPager = findViewById(R.id.pager)
        // Create the adapter that will return a fragment for each of the primary sections of the app.
        mAppSectionsPagerAdapter = AppSectionsPagerAdapter(supportFragmentManager, context)
        mViewPager.setAdapter(mAppSectionsPagerAdapter)

        // Setup the TabLayout and connect it to the ViewPager.
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        // setupWithViewPager() does not work very well, so we do all the things oneself (see below)
        //tabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // When the given tab is selected, switch to the corresponding page in the ViewPager.
                val position = tab.position
                mViewPager.setCurrentItem(position)
                if (position == 1) {
                    // show FloatingActionButton
                    mFab.show()
                    // show special menu entries to be more responsive
                    // Needs to be set in onPrepareOptionsMenu() as well!
                    mMainMenu?.findItem(R.id.action_filter)?.setVisible(true)
                } else {
                    // tab position != 1
                    // hide FloatingActionButton
                    mFab.hide()
                    // hide special menu entries to be more responsive
                    mMainMenu?.findItem(R.id.action_filter)?.setVisible(false)
                    // ensure, that ActionBar is shown
                    supportActionBar?.show()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        // For each of the sections in the app, add a tab to the action bar.
        for (i in 0..<mAppSectionsPagerAdapter.count) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            val tab = tabLayout.newTab()
            tab.setCustomView(R.layout.custom_tab_title)
            val tabView = checkNotNull(tab.customView)
            val tabTitle = tabView.findViewById<TextView>(R.id.title)
            val img = tabView.findViewById<ImageView>(R.id.icon)
            tabTitle.text = mAppSectionsPagerAdapter.getPageTitle(i)
            val icon = mAppSectionsPagerAdapter.getPageIcon(i)
            if (icon != 0) img.setImageResource(mAppSectionsPagerAdapter.getPageIcon(i))
            tabLayout.addTab(tab)
        }


        // setup the navigation drawer view from android.support.v4.widget.DrawerLayout for API < 21.
        mDrawerLayout = findViewById(R.id.drawerlayout)
        // Necessary for automatically animated navigation drawer upon open and close.
        // The two strings are not displayed to the user.
        val toggle: ActionBarDrawerToggle =
            object : ActionBarDrawerToggle(this,
                mDrawerLayout,
                supportToolbar,
                R.string.drawer_open,
                R.string.drawer_close) {
                override fun onDrawerStateChanged(newState: Int) {
                    mDrawerState = newState
                }
            }
        mDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.navigation_drawer)
        navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onStart() {
        super.onStart()
        Log.v(LOG_TAG, "onStart()")

        // request notification permission on Android 13 and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    Constants.PERMISSION_REQUEST_POST_NOTIFICATIONS)
            }
        }

        // request manage external storage permission to backup and restore database
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // For Android 11 (API level 30) and above, you need to request this permission using an intent to open the system settings
                Log.v(LOG_TAG, "onStart(): Permission MANAGE_EXTERNAL_STORAGE not granted")
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.setData(uri)
                startActivity(intent)
                return
            }
            Log.v(LOG_TAG, "onStart(): Permission MANAGE_EXTERNAL_STORAGE granted")
        } else {
            // check if we have permission to read from external storage (needed by MQTT to read cert-file)
            // and write (needed to write stacktrace if app crashed)
            // callback method onRequestPermissionsResult gets the result of the request.
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                Constants.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE)
        }
    }

    /* ******************************************************************
	 * FragmentPagerAdapter to handle tabs
	 * ****************************************************************** */
    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    class AppSectionsPagerAdapter(fm: FragmentManager, private val mContext: Context) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> mMainSectionFragment
                1 -> mViewSectionFragment
                2 -> SettingsSectionFragment()
                else ->                // Unknown section number. Throw exception.
                    throw RuntimeException("Section item " + position + "unknown")
            }
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence {
            when (position) {
                0 -> return mContext.resources.getString(R.string.tab_main)
                1 -> return mContext.resources.getString(R.string.tab_view)
                2 -> return mContext.resources.getString(R.string.tab_settings)
            }
            return ""
        }

        fun getPageIcon(position: Int): Int {
            when (position) {
                0 -> return R.drawable.ic_tab_start
                1 -> return R.drawable.ic_tab_view
                2 -> return R.drawable.ic_tab_settings
            }
            return 0
        }
    }


    /* ******************************************************************
	 * Options menu stuff (onOptionsItemSelected)
	 * ****************************************************************** */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        mMainMenu = menu // store the menu in a local variable

        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // Prepare the Screen's standard options menu to be displayed. 
        // This is called right before the menu is shown, every time it is shown. You can use this
        // method to efficiently enable/disable items or otherwise dynamically modify the contents.

        // show start/end action button
        if (mTW.workStarted) {
            menu.findItem(R.id.action_start).setVisible(false)
            menu.findItem(R.id.action_end).setVisible(true)
        } else {
            menu.findItem(R.id.action_start).setVisible(true)
            menu.findItem(R.id.action_end).setVisible(false)
        }
        // show special menu entries only on view tab
        menu.findItem(R.id.action_filter).setVisible(mViewPager.currentItem == 1)
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        // Handle presses on the action bar items
        if (id == R.id.action_start || id == R.id.action_end) {
            // make the broadcast Intent explicit by specifying the receiver class
            sendBroadcast(Intent(Constants.RECEIVER_START_STOP, null, this, StartStopReceiver::class.java))
            return true
        } else if (id == R.id.action_filter) {
            // prepare to edit filter
            val filterFrag = FilterFragment()
            filterFrag.setPickerMonth(mTW.filterMonth)
            filterFrag.setPickerYear(mTW.filterYear)
            // Display the edit fragment as the main content.
            supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(0, filterFrag)
                .commit()
            return true
        }
        return super.onOptionsItemSelected(item)
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
*//* ******************************************************************
	 * Navigation drawer menu stuff (onNavigationItemSelected)
	 * ****************************************************************** */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Close drawer on item click
        mDrawerLayout.closeDrawers()

        // Get item ID to determine what to do on user click
        val itemId = item.itemId
        when (itemId) {
            R.id.nav_item_add_times -> {
                // prepare to add a new SQL row
                val currentTime = TimeUtil.getCurrentTimeInMillis()
                val times = Times(-1, currentTime, currentTime, false)

                // prepare to edit entry
                val frag = EditTimesFragment()
                frag.setTimes(times)
                // Display the edit fragment as the main content.
                supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .add(0, frag).commit()
            }

            R.id.nav_item_backup -> {
                // backup internal database to external file (permission must be granted in onStart())
                mViewSectionFragment.exportDatabase(mBackupDbPath)
            }

            R.id.nav_item_restore -> {
                val alertDialogBuilder = AlertDialog.Builder(this)
                var message = resources.getText(R.string.confirm_restore)
                    .toString() // text must have a %s to insert the backup path
                message = String.format(Locale.getDefault(), message, mBackupDbPath)

                // set dialog message
                alertDialogBuilder.setMessage(message).setCancelable(true)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        // if "Yes" this button is clicked
                        // restore backup database to internal database (permission must be granted in onStart())
                        mViewSectionFragment.importDatabase(mBackupDbPath)
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ -> // just close the dialog box and do nothing
                        dialog.cancel()
                    }

                // create alert dialog and show it
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }

            R.id.nav_item_about -> {
                // Display the about fragment as the main content.
                supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .add(0, AboutFragment()).commit()
            }
        }
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // if drawer is open, close it instead of closing the app
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /* ******************************************************************
	 * All listener of class MainActivity
	 * ****************************************************************** */
    private var mIsPinching = false

    /**
     * Called to process touch screen events.
     * Need this instead of view.setOnTouchListener(...) because of ScrollView layout.
     * See OnSwipeTouchListener call in class MainSectionFragment
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        var result = false

        // detect pinching
        val pointerCount = event.pointerCount
        val action = event.action
        if (pointerCount > 1) mIsPinching = true
        if (pointerCount == 1 && action == MotionEvent.ACTION_UP) mIsPinching = false

        //Log.d(LOG_TAG, String.format("pointerCount=%d, mIsPinching=%b, action=%d", pointerCount, mIsPinching, action));
        if (mViewPager.currentItem == 0) { // only in first tab (START)
            val onSwipeTouchListener = mMainSectionFragment.onSwipeTouchListener
            // decide if we should swipe the view
            if (!mIsPinching &&  // do not if pinching is active
                mDrawerState == DrawerLayout.STATE_IDLE &&  // only if drawer is not moving
                !mDrawerLayout.isDrawerOpen(GravityCompat.START))  // only if drawer is closed
            {
                result = onSwipeTouchListener.onTouch(onSwipeTouchListener.view, event)
            } else if (mDrawerState == DrawerLayout.STATE_DRAGGING) {
                // terminate swipe if drawer opens by faking an action up event ...
                event.action = MotionEvent.ACTION_UP
                onSwipeTouchListener.onTouch(onSwipeTouchListener.view, event)
                event.action = action
            }
        }
        if (!result) {
            result = super.dispatchTouchEvent(event)
        }
        return result
    }

    // EditTimes Listener
    override fun onFinishEditTimesFragment(id: Int, times: Times) {
        mTimes = times
        var selection: String? = null
        val context = applicationContext

        when (id) {
            Constants.BUTTON_DELETE -> {
                // The user wants to deleted the date/time. Asked for confirmation.
                // Do not use an AlertDialog.Builder directly, because it doesn't get recreated
                // if app gets restarted (after pressing home or rotating)
                val confirmDialog = ConfirmDialogFragment()
                confirmDialog.setMessage(resources.getText(R.string.button_delete).toString() + " " + times.dateString)
                confirmDialog.show(supportFragmentManager, "ConfirmDialogFragment")
            }

            Constants.BUTTON_OK -> {
                // The user selected a new date/time. Now update that.
                if (times.id == -1L) {
                    // new entry
                    mViewSectionFragment.addTimesItem(times)
                    selection = resources.getString(R.string.database_add_positive_result)
                } else {
                    // update existing entry
                    mViewSectionFragment.updateTimesItem(times)
                    selection = resources.getString(R.string.database_update_positive_result)
                }
                // update statistics, it might change ...
                mMainSectionFragment.updateStatisticsView()
                mTimes = null // mark as handled
            }

            Constants.BUTTON_CANCEL ->            // user canceled action (currently not used / called)
                mTimes = null // mark as handled
        }

        if (selection != null) {
            Toast.makeText(context, selection, Toast.LENGTH_SHORT).show()
        }
    }

    // ConfirmDialogFragment Listener
    override fun onConfirmDialogYes() {
        // user acknowledged the deletion
        mViewSectionFragment.deleteTimesItem(mTimes)
        // update statistics, it might change ...
        mMainSectionFragment.updateStatisticsView()
        // if work was started it might change the day ...
        if (mTW.workStarted) {
            mMainSectionFragment.updateTimesView()
            // reset alarms and notification
            AlarmUtils.setAlarms(this, mTW)
        }
        Toast.makeText(this, R.string.database_delete_positive_result, Toast.LENGTH_SHORT).show()

        mTimes = null // mark as handled
    }

    override fun onConfirmDialogNo() {
        // user declined the deletion
        mTimes = null // mark as handled
    }

    // Filter Listener
    override fun onFinishFilterFragment(id: Int, month: Int, year: Int) {
        when (id) {
            Constants.BUTTON_OK, Constants.BUTTON_CLEAR -> {
                // The user selected a new date/time. Update the filter
                mViewSectionFragment.updateFilter(month, year)
            }

            Constants.BUTTON_CANCEL -> {}
        }
    }

    fun onCheckboxClicked(view: View) {
        // Check which checkbox was clicked
        if (view.id == R.id.homeoffice_cb) {
            // save the Home Office check box into mTW dataset
            mTW.homeOffice = (view as CheckBox).isChecked
            if (mTW.workStarted) {
                mMainSectionFragment.updateTimesView()
                // reset alarms and notification
                AlarmUtils.setAlarms(this, mTW)
            }
        } else {
            throw IllegalStateException("Unexpected value: " + view.id)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun showDatePickerDialog(v: View?) {
        if (mTW.workStarted) {
            // do not set mTW.timeStart to current time, otherwise we can not cancel ...
            val cal = TimeUtil.getCurrentTime()
            if (mTW.timeStart != -1L) cal.timeInMillis = mTW.timeStart
            val datePicker = DatePickerFragment()
            datePicker[cal] = R.string.date_text
            datePicker.show(supportFragmentManager, "datePicker")
        } else {
            Toast.makeText(this, R.string.work_not_started, Toast.LENGTH_LONG).show()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun showTimePickerDialogStartTime(v: View?) {
        if (mTW.workStarted) {
            // do not set mTW.timeStart to current time, otherwise we can not cancel ...
            val cal = TimeUtil.getCurrentTime()
            if (mTW.timeStart != -1L) cal.timeInMillis = mTW.timeStart
            val timePicker = TimePickerFragment()
            timePicker[cal] = R.string.start_time_set
            timePicker.show(supportFragmentManager, "timePicker")
        } else {
            Toast.makeText(this, R.string.work_not_started, Toast.LENGTH_LONG).show()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun showTimePickerDialogEndTime(v: View?) {
        if (mTW.workStarted) {
            // do not set mTW.timeEnd to current time, otherwise we can not cancel ...
            val cal = TimeUtil.getCurrentTime()
            if (mTW.timeEnd != -1L) cal.timeInMillis = mTW.timeEnd
            val timePicker = TimePickerFragment()
            timePicker[cal] = R.string.end_time_set
            timePicker.show(supportFragmentManager, "timePicker")
        } else {
            Toast.makeText(this, R.string.work_not_started, Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("NonConstantResourceId")
    override fun onFinishTimePickerFragment(cal: Calendar, titleId: Int) {
        // set time depending on titleId
        if (titleId == R.string.start_time_set) {
            val calTimeStart = TimeUtil.getCurrentTime()
            if (mTW.timeStart != -1L) calTimeStart.timeInMillis = mTW.timeStart
            calTimeStart[Calendar.HOUR_OF_DAY] = cal[Calendar.HOUR_OF_DAY]
            calTimeStart[Calendar.MINUTE] = cal[Calendar.MINUTE]
            mTW.calStart = calTimeStart
        } else if (titleId == R.string.end_time_set) {
            val calTimeEnd = TimeUtil.getCurrentTime()
            if (mTW.timeEnd != -1L) calTimeEnd.timeInMillis = mTW.timeEnd
            calTimeEnd[Calendar.HOUR_OF_DAY] = cal[Calendar.HOUR_OF_DAY]
            calTimeEnd[Calendar.MINUTE] = cal[Calendar.MINUTE]
            mTW.calEnd = calTimeEnd
        }
        // update view and alarms ...
        mMainSectionFragment.updateTimesView()
        AlarmUtils.setAlarms(this, mTW)
    }

    @SuppressLint("NonConstantResourceId")
    override fun onFinishDatePickerFragment(cal: Calendar, titleId: Int) {
        // set time depending on titleId
        if (titleId == R.string.date_text) {
            val calTimeStart = TimeUtil.getCurrentTime()
            if (mTW.timeStart != -1L) calTimeStart.timeInMillis = mTW.timeStart
            calTimeStart[cal[Calendar.YEAR], cal[Calendar.MONTH]] = cal[Calendar.DAY_OF_MONTH]
            mTW.calStart = calTimeStart
            if (mTW.timeEnd != -1L) {
                val calTimeEnd = mTW.calEnd
                calTimeEnd[cal[Calendar.YEAR], cal[Calendar.MONTH]] = cal[Calendar.DAY_OF_MONTH]
                mTW.calEnd = calTimeEnd
            }
        } else {
            throw IllegalStateException("Unexpected value: $titleId")
        }
        // update view and alarms ...
        mMainSectionFragment.updateTimesView()
        mMainSectionFragment.updateStatisticsView()
        AlarmUtils.setAlarms(this, mTW)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Handle the permissions request response.
        when (requestCode) {
            Constants.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE ->
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_TAG, "onRequestPermissionsResult(): WRITE_EXTERNAL_STORAGE granted")
                    // permission was granted, do the backup right now
                    //mViewSectionFragment.exportDatabase(mBackupDbPath)
                } else {
                    Log.e(LOG_TAG, "onRequestPermissionsResult(): WRITE_EXTERNAL_STORAGE denied")
                }

            Constants.PERMISSION_REQUEST_READ_EXTERNAL_STORAGE ->
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_TAG, "onRequestPermissionsResult(): READ_EXTERNAL_STORAGE granted")
                    // permission was granted, do the restore right now
                    //mViewSectionFragment.importDatabase(mBackupDbPath)
                } else {
                    Log.e(LOG_TAG, "onRequestPermissionsResult(): READ_EXTERNAL_STORAGE denied")
                }

            Constants.PERMISSION_REQUEST_POST_NOTIFICATIONS ->
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Log.d(LOG_TAG, "onRequestPermissionsResult(): POST_NOTIFICATIONS granted")
                } else {
                    Log.e(LOG_TAG, "onRequestPermissionsResult(): POST_NOTIFICATIONS denied")
                }

            Constants.PERMISSION_REQUEST_EXACT_ALARM ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Log.d(LOG_TAG, "onRequestPermissionsResult(): SCHEDULE_EXACT_ALARM granted")
                } else {
                    Log.e(LOG_TAG, "onRequestPermissionsResult(): SCHEDULE_EXACT_ALARM denied")
                }
        }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState)
        if (mTimes != null) {
            // Save the user's current selected time
            mTimes!!.saveInstanceState(savedInstanceState)
        }
    }

    public override fun onResume() {
        super.onResume()
        // remember, that activity is resumed
        CustomApplication.activityResumed()
    }

    public override fun onPause() {
        super.onPause()
        // remember, that activity is paused
        CustomApplication.activityPaused()
        // Store TimesWork data in persistent store
        mTW.saveTimesWork()
    }

    public override fun onDestroy() {
        super.onDestroy()
        Log.v(LOG_TAG, "onDestroy()")
        // Unregister receiver, because after that activity is killed!
        unregisterReceiver(receiverRecreate)
        unregisterReceiver(receiverUpdateView)
        unregisterReceiver(alarmReceiver)
        //  clean up stored references to avoid leaking
        mTimes = null
        mMainMenu = null
    }

    companion object {
        private val LOG_TAG: String by lazy { MainActivity::class.java.simpleName }

        @JvmField
        var mMainSectionFragment: MainSectionFragment = MainSectionFragment()

        @JvmField
        var mViewSectionFragment: ViewSectionFragment = ViewSectionFragment()
    }
}
