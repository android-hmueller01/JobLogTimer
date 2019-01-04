# JobLogTimer

The Android App JobLogTimer keeps track of the time you work and alarms your to go home.

# ChangeLog

V0.15: versionCode 13, 04.01.2019 Mr
- Added current time display (including seconds)
- Added release signing config
- Updated targetSdkVersion to 28 (Android 9 / PIE)

V0.14: versionCode 12, 17.07.2018 Mr
- Fixed issues with Android 8.0 / Oreo (Widget, Notifications, Alarms, Services).<br />
  It felt like nothing works any more ... :-(
- Fixed bug of normal work time calculation, if break time is not within work time.

V0.13: versionCode 11, 13.02.2018 Mr
- Migrated project to Android Studio
- Updated minSdkVersion to 14 and targetSdkVersion to 27 (Android 8.1 / OREO)
- Switched from Locale.US to Locale.getDefault()
- Fixed a bug when work started or ended at fix break time (wrong calc)
- Fixed some (not all) warnings by Android Studio

V0.12: versionCode 10, 18.10.2017 Mr
- Fixed ic_launcher_bw notification icon for V21 (Lollipop) and later.
  Must be a 1 bit depth graphics to look good.
- Getting app version name from Manifest instead of a string resource

V0.11: versionCode 9, 18.01.2017 Mr
- Fixed widget height on some devices (changed size of preview image)
- Setting start date, time and end time is now only possible if work is started
- Hiding ActionBar in view section only, if we have at least double amount of
  entries than visible entries
- Fixed behavior when drawer is opened with a swipe within statistics view (which
  can be swiped as well)

V0.10: versionCode 8, 12.01.2017 Mr
- App is now more material design like
- Added navigation drawer and moved tasks from menu
- Added floating action button on view section to add times
- Changed behavior of hiding and showing Toolbar/Actionbar on view section
- Made showing and hiding special menu entries more responsive (added to onTabSelected())
- Fixed edit times layout for V21 (Lollipop) and later, by forcing spinner mode

V0.9: versionCode 7, 24.12.2016 Mr
- Updated targetSdkVersion to 25 (Android 7.1.1 / NOUGAT)
- Migrated from ActionbarSherlock to AppCompat (lots of changes ...)
- Using Toolbar layout with AppCompat in new colors (blue)
- Updated swiping statistics to OnSwipeTouchListener (also used by Solar app)
- Removed (comment out) donation button in preferences, as for the
  moment there will be no playstore version
- Added backup and restore menu entries in preferences tab to save
  and reload database from a local file (e.g. to move between devices)
- Asking for permissions if device is running SDK >= 23 (Marshmallow)
- Added long press on times list to delete that item
- Bugfix: wrong calculation of work times outside break time, for fixed break time

V0.8: versionCode 6, 01.09.2015 Mr
- Added donation button in preferences
- Changed color of +/- markers (thanks Robert)
- Added disable widget update under low battery state (see settings)
- Restoring alarms after device booted (needs permission RECEIVE_BOOT_COMPLETED)
- Fixed min. distance of swipe with different screen resolutions
- Fixed view filter, not showing last entry if last day of month

V0.7: versionCode 5, 08.02.2015 Mr
- Added widget (shows work times and start/stop button, lots of changes ...)
- Added option for break times based on German law
- Added +/- markers when swiping statistics
- Added animation on statistics after swiping (used OvershootInterpolator)
- Changed swipe direction of statistics
- Changed action bar start/stop buttons to service calls
- Updated app icon to new design
- Fixed that app sometimes got incorrect started from notification

V0.6: versionCode 4, 29.01.2015 Mr
- Reanimated menu button (did not work since revised action bar buttons)
- End work can now also be pressed from notification directly
- Added "UPDATE_VIEW" broadcast receiver to activity and fragments

V0.5: versionCode 3, 25.01.2015 Mr
- Added filter function on view (with the restriction that app runs only on V11
  (Honeycomb 3.0) and later, because we have no build in number picker before that) 
- Updated layout of Start screen
- Added tab icons (Start and View)
- Revised action bar buttons (e.g. start/stop on all tabs)
- All statistics can now handle multiple entries per day
- Preparation of notification action button, but still disabled
- Changed file comments suitable for Apache License and SVN keywords

V0.4: versionCode 2, 03.01.2015 Mr
- Fixed bug with Date and Time Picker (e.g. on Samsung devices,
  not seen on Emulator nor LG G2). Date and Time could not be set.

V0.3: versionCode 1, 17.12.2014 Mr
- Updated layout of Start section
- Added weekly and monthly statistics
- Added statistics in view section
- Added swiping in statistics to change week
- Added UI option
- Fixed an issue, when canceling the date and time picker
- Fixed some issues with memory leaking (nulling globals)

V0.2: versionCode 1, 08.12.2014 Mr
- Updated START bitmap to new icon
- "pref_break_atfixtime_key" gets enabled/disabled inverse to
  "pref_break_after_hours_enable_key"
- fixed bug in calculation of worked time, when 
  "pref_break_after_hours_enable_key" is disabled
  
V0.1: versionCode 1, 04.12.2014 Mr
- Initial release


# References

Icon generator: http://romannurik.github.io/AndroidAssetStudio

Support library: https://android-developers.googleblog.com/2015/05/android-design-support-library.html

AppCompat: https://android-developers.googleblog.com/2014/10/appcompat-v21-material-design-for-pre.html
