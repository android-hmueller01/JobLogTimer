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
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.euhm.jlt.dao.Times
import de.euhm.jlt.dao.TimesDataSource
import de.euhm.jlt.dao.TimesWork
import de.euhm.jlt.database.JobLogContract
import de.euhm.jlt.dialogs.ConfirmDialogFragment
import de.euhm.jlt.dialogs.ConfirmDialogFragment.YesNoListener
import de.euhm.jlt.dialogs.DatePickerFragment.OnDatePickerFragmentListener
import de.euhm.jlt.dialogs.EditTimesFragment
import de.euhm.jlt.dialogs.EditTimesFragment.OnEditTimesFragmentListener
import de.euhm.jlt.dialogs.FilterFragment
import de.euhm.jlt.dialogs.FilterFragment.OnFilterFragmentListener
import de.euhm.jlt.dialogs.TimePickerFragment.OnTimePickerFragmentListener
import de.euhm.jlt.receivers.AlarmReceiver
import de.euhm.jlt.receivers.StartStopReceiver
import de.euhm.jlt.utils.AlarmUtils
import de.euhm.jlt.utils.Constants
import de.euhm.jlt.utils.TimeUtil
import java.io.File
import java.util.Calendar
import java.util.Locale

private val LOG_TAG: String = MainActivity::class.java.simpleName

/**
 * Main activity of JobLog
 * @author hmueller
 */
class MainActivity : AppCompatActivity(), OnNavigationItemSelectedListener, OnEditTimesFragmentListener,
    YesNoListener, OnDatePickerFragmentListener, OnTimePickerFragmentListener, OnFilterFragmentListener {
    private var mTimes: Times? = null // temp. Times for different dialogs
    private var mMainMenu: Menu? = null // saved MainMenu for later use in onKeyUp() and onTabSelected()
    private lateinit var mTimesWork: TimesWork
    private lateinit var mBackupDbPath: String

    /**
     * The [AppSectionsPagerAdapter] that will provide fragments for each of the
     * primary sections of the app. We use a [FragmentStateAdapter]
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a FragmentStatePagerAdapter.
     */
    private lateinit var mAppSectionsPagerAdapter: AppSectionsPagerAdapter

    /**
     * The [ViewPager] that will display the primary sections of the app, one at a
     * time.
     */
    private lateinit var mViewPager: ViewPager2

    /**
     * The [DrawerLayout] that will provide full
     * material design drawer navigation back to Android v4.
     */
    private lateinit var mDrawerLayout: DrawerLayout
    private var mDrawerState: Int = 0 // state of drawer (s. e.g. DrawerLayout.STATE_SETTLING)

    /**
     * Used by onBackPressedDispatcher to implement the onBackPressed callback
     */
    private lateinit var onBackPressedCallback: OnBackPressedCallback

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

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun myRegisterReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = applicationContext
        setContentView(R.layout.activity_main)

        // register the recreate receiver to restart activity from service
        myRegisterReceiver(receiverRecreate, IntentFilter(Constants.RECEIVER_RECREATE))
        // register the update view receiver to update view from service
        myRegisterReceiver(receiverUpdateView, IntentFilter(Constants.RECEIVER_UPDATE_VIEW))
        // register the alarm receiver
        myRegisterReceiver(alarmReceiver, IntentFilter(Constants.RECEIVER_NORMAL_WORK_ALARM))
        myRegisterReceiver(alarmReceiver, IntentFilter(Constants.RECEIVER_MAX_WORK_ALARM))

        // set path and name of backup database used by db export and import
        // set path to Android/data/de.euhm.jlt/files to avoid permission requests
        // mBackupDbPath = getExternalFilesDir(null)!!.absolutePath + File.separatorChar + JobLogContract.DATABASE_NAME
        // path to external storage directory root with name of app
        mBackupDbPath = Environment.getExternalStorageDirectory().absolutePath + //
                File.separatorChar + resources.getString(R.string.app_name) + //
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

        mTimesWork = TimesWork(context)
        //mTimesWork.timeEnd = -1L // only for debugging, resetting End time

        // Setup FloatingActionButton from android.support.design.widget.FloatingActionButton for API < 21.
        mFab = findViewById(R.id.fab)
        // handle the FloatingActionButton click event
        mFab.setOnClickListener { // prepare to add a new SQL row
            val currentTime = TimeUtil.getCurrentTimeInMillis()
            val times = Times(-1, currentTime, currentTime, false)


            // prepare to edit entry
            val frag = EditTimesFragment().setTimes(times)
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
        mAppSectionsPagerAdapter = AppSectionsPagerAdapter(this, context)
        mViewPager.adapter = mAppSectionsPagerAdapter

        // Setup swiping on statistics layout
        val recyclerView = mViewPager.getChildAt(0) as RecyclerView
        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (mViewPager.currentItem == 0) {
                    val root = rv.rootView
                    val scrollView: View = root.findViewById(R.id.main_scroll_view)
                    val statsView: View = root.findViewById(R.id.layout_main_statistics)
                    val y: Int = e.y.toInt() + scrollView.scrollY

                    if (y in statsView.top..statsView.bottom) {
                        // Block swipe in this vertical region
                        return true
                    }
                }
                return false // Let ViewPager2 handle it
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                // no operation
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                // no operation
            }
        })

        mViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // This method is called when a new page becomes selected.
                // The 'position' parameter indicates the index of the selected page (starting from 0).
                mViewPager.currentItem = position
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
        })

        // Setup the TabLayout and connect it to the ViewPager.
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        TabLayoutMediator(tabLayout, mViewPager) { tab, position ->
            // Create a tab with text corresponding to the page title defined by the adapter.
            tab.setCustomView(R.layout.custom_tab_title)
            val tabView = tab.customView
            val tabTitle: TextView? = tabView?.findViewById(R.id.title)
            val tabIcon: ImageView? = tabView?.findViewById(R.id.icon)
            tabTitle?.text = mAppSectionsPagerAdapter.getPageTitle(position)
            tabIcon?.setImageResource(mAppSectionsPagerAdapter.getPageIcon(position))
            //tab.text = mAppSectionsPagerAdapter.getPageTitle(position)
            //tab.icon = ContextCompat.getDrawable(this, mAppSectionsPagerAdapter.getPageIcon(position))
        }.attach()

        // setup the navigation drawer view from android.support.v4.widget.DrawerLayout for API < 21.
        mDrawerLayout = findViewById(R.id.drawerlayout)
        // Necessary for automatically animated navigation drawer upon open and close.
        // The two strings are not displayed to the user.
        val toggle: ActionBarDrawerToggle = object :
            ActionBarDrawerToggle(this, mDrawerLayout, supportToolbar, R.string.drawer_open, R.string.drawer_close) {
            override fun onDrawerStateChanged(newState: Int) {
                mDrawerState = newState
            }
        }
        mDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.navigation_drawer)
        navigationView.setNavigationItemSelectedListener(this)

        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // if drawer is open, close it instead of closing the app
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // Let the system handle the default back behavior (e.g., finish the activity)
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onStart() {
        super.onStart()
        Log.v(LOG_TAG, "onStart()")

        checkAndRequestPermissions()
    }

    /**
     * Backup internal database to local storage.
     *
     * Previous backup will be overwritten without warning!
     *
     * ExternalStorageManager permission is required.
     *
     * @param dbPath Path to export to.
     */
    private fun exportDatabase(dbPath: String) {
        // get Times database
        val db = TimesDataSource(this)
        try {
            val result = db.dbHelper.exportDatabase(dbPath)
            if (result) {
                Toast.makeText(this, "Backed up database to $dbPath", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Failed to backup database.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to backup database.", Toast.LENGTH_LONG).show()
        }
        db.close()
    }

    /**
     * Restore internal database from local storage (backup).
     *
     * ***WARNING:** This will delete the current database and replace it
     * with the backup!*
     *
     * ExternalStorageManager permission is required.
     *
     * @param dbPath Path to import from
     */
    private fun importDatabase(dbPath: String) {
        // get Times database
        val db = TimesDataSource(this)
        try {
            val result = db.dbHelper.importDatabase(dbPath)
            if (result) {
                // update views that changes take place
                sendBroadcast(Intent(Constants.RECEIVER_UPDATE_VIEW).setPackage(packageName))

                Toast.makeText(this, "Imported database $dbPath", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Failed to import database $dbPath", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to import database $dbPath", Toast.LENGTH_LONG).show()
        }
        db.close()
    }

    // Register the activity result launcher for multiple permissions
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { entry ->
                val permissionName = entry.key
                val isGranted = entry.value
                when (permissionName) {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                        if (isGranted) {
                            Log.d(LOG_TAG, "$permissionName granted")
                            // Proceed with storage-related functionality
                        } else {
                            Log.d(LOG_TAG, "$permissionName denied")
                            // Explain to the user why the feature is unavailable
                        }
                    }
                    // Handle other permissions here
                    else -> {
                        if (isGranted) {
                            Log.d(LOG_TAG, "$permissionName granted")
                        } else {
                            Log.d(LOG_TAG, "$permissionName denied")
                        }
                    }
                }
            }
        }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // request notification permission on Android 13 and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.USE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.USE_EXACT_ALARM)
            }
        }

        // below Android 11 (API level 30), you need to request permission
        // to read and write from external storage (needed for backup)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        // If there are permissions to request, launch the permission request
        if (permissionsToRequest.isNotEmpty()) {
            requestMultiplePermissions.launch(permissionsToRequest.toTypedArray())
        } else {
            // All permissions already granted, proceed with functionality
            Log.d(LOG_TAG, "All permissions already granted")
            // You can now safely use the features requiring the permissions
        }
    }

    private fun checkAndRequestExternalStorageManager() {
        // request manage external storage permission to backup and restore database
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // For Android 11 (API level 30) and above, you need to request this permission using an intent to open the system settings
                Log.v(LOG_TAG,
                    "checkAndRequestExternalStorageManager(): Permission MANAGE_EXTERNAL_STORAGE not granted. Requesting ...")
                val uri = Uri.fromParts("package", packageName, null)
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).setData(uri).apply {
                    startActivity(this)
                }
            } else {
                Log.v(LOG_TAG, "checkAndRequestExternalStorageManager(): Permission MANAGE_EXTERNAL_STORAGE granted")
            }
        }
    }

    /* ******************************************************************
     * FragmentStateAdapter to handle tabs
     * ****************************************************************** */
    /**
     * A [FragmentStateAdapter] that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    class AppSectionsPagerAdapter(fragmentActivity: FragmentActivity, private val mContext: Context) :
        FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> mMainSectionFragment // MainSectionFragment()
                1 -> mViewSectionFragment // ViewSectionFragment()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }

        override fun getItemCount(): Int = 2

        fun getPageTitle(position: Int): CharSequence {
            when (position) {
                0 -> return mContext.resources.getString(R.string.tab_main)
                1 -> return mContext.resources.getString(R.string.tab_view)
            }
            return ""
        }

        fun getPageIcon(position: Int): Int {
            when (position) {
                0 -> return R.drawable.ic_tab_start
                1 -> return R.drawable.ic_tab_view
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
        if (mTimesWork.workStarted) {
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
            filterFrag.setPickerMonth(mTimesWork.filterMonth)
            filterFrag.setPickerYear(mTimesWork.filterYear)
            // Display the edit fragment as the main content.
            supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(0, filterFrag).commit()
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
    */

    /* ******************************************************************
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
                val frag = EditTimesFragment().setTimes(times)
                // Display the edit fragment as the main content.
                supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .add(0, frag).commit()
            }

            R.id.nav_item_backup -> {
                checkAndRequestExternalStorageManager()
                val alertDialogBuilder = AlertDialog.Builder(this)
                var message = resources.getText(R.string.confirm_backup)
                    .toString() // text must have a %s to insert the backup path
                message = String.format(Locale.getDefault(), message, mBackupDbPath)

                // set dialog message
                alertDialogBuilder.setMessage(message).setCancelable(true)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        // if "Yes" this button is clicked
                        // backup database to external file (permission must be granted)
                        exportDatabase(mBackupDbPath)
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ -> // just close the dialog box and do nothing
                        dialog.cancel()
                    }

                // create alert dialog and show it
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }

            R.id.nav_item_restore -> {
                checkAndRequestExternalStorageManager()
                val alertDialogBuilder = AlertDialog.Builder(this)
                var message = resources.getText(R.string.confirm_restore)
                    .toString() // text must have a %s to insert the backup path
                message = String.format(Locale.getDefault(), message, mBackupDbPath)

                // set dialog message
                alertDialogBuilder.setMessage(message).setCancelable(true)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        // if "Yes" this button is clicked
                        // restore backup database to internal database (permission must be granted)
                        importDatabase(mBackupDbPath)
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ -> // just close the dialog box and do nothing
                        dialog.cancel()
                    }

                // create alert dialog and show it
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }

            R.id.nav_item_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }

            R.id.nav_item_about -> {
                // Display the about fragment as the main content.
                supportFragmentManager
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .add(0, AboutFragment())
                    .commit()
            }
        }
        return true
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
                confirmDialog.setMessage(resources.getText(R.string.button_delete)
                    .toString() + " " + times.dateString)
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
                val workedTimeDay = TimeUtil.getFinishedDayWorkTime(context, TimeUtil.getCurrentTime())
                mTimesWork.timeWorked = workedTimeDay
                mMainSectionFragment.updateTimesView()
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
        val workedTimeDay = TimeUtil.getFinishedDayWorkTime(applicationContext, TimeUtil.getCurrentTime())
        mTimesWork.timeWorked = workedTimeDay
        mMainSectionFragment.updateTimesView()
        mMainSectionFragment.updateStatisticsView()
        // if work was started it might change the day ...
        if (mTimesWork.workStarted) {
            // reset alarms and notification
            AlarmUtils.setAlarms(this)
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

    override fun onFinishTimePickerFragment(cal: Calendar, titleId: Int) {
        // set time depending on titleId
        if (titleId == R.string.start_time_set) {
            val calTimeStart = TimeUtil.getCurrentTime()
            if (mTimesWork.timeStart != -1L) calTimeStart.timeInMillis = mTimesWork.timeStart
            calTimeStart[Calendar.HOUR_OF_DAY] = cal[Calendar.HOUR_OF_DAY]
            calTimeStart[Calendar.MINUTE] = cal[Calendar.MINUTE]
            mTimesWork.calStart = calTimeStart
        } else if (titleId == R.string.end_time_set) {
            val calTimeEnd = TimeUtil.getCurrentTime()
            if (mTimesWork.timeEnd != -1L) calTimeEnd.timeInMillis = mTimesWork.timeEnd
            calTimeEnd[Calendar.HOUR_OF_DAY] = cal[Calendar.HOUR_OF_DAY]
            calTimeEnd[Calendar.MINUTE] = cal[Calendar.MINUTE]
            mTimesWork.calEnd = calTimeEnd
        }
        // update view and alarms ...
        mMainSectionFragment.updateTimesView()
        AlarmUtils.setAlarms(this)
    }

    override fun onFinishDatePickerFragment(cal: Calendar, titleId: Int) {
        // set time depending on titleId
        if (titleId == R.string.date_text) {
            val calTimeStart = TimeUtil.getCurrentTime()
            if (mTimesWork.timeStart != -1L) calTimeStart.timeInMillis = mTimesWork.timeStart
            calTimeStart[cal[Calendar.YEAR], cal[Calendar.MONTH]] = cal[Calendar.DAY_OF_MONTH]
            mTimesWork.calStart = calTimeStart
            if (mTimesWork.timeEnd != -1L) {
                val calTimeEnd = mTimesWork.calEnd
                calTimeEnd[cal[Calendar.YEAR], cal[Calendar.MONTH]] = cal[Calendar.DAY_OF_MONTH]
                mTimesWork.calEnd = calTimeEnd
            }
        } else {
            throw IllegalStateException("Unexpected value: $titleId")
        }
        // update view and alarms ...
        mMainSectionFragment.updateTimesView()
        mMainSectionFragment.updateStatisticsView()
        AlarmUtils.setAlarms(this)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState)
        if (mTimes != null) {
            // Save the user's current selected time
            mTimes!!.saveInstanceState(outState)
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
        var mMainSectionFragment: MainSectionFragment = MainSectionFragment()

        var mViewSectionFragment: ViewSectionFragment = ViewSectionFragment()
    }
}
