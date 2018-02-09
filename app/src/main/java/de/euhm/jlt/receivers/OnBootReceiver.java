/**
 * $Id: OnBootReceiver.java 122 2015-03-06 19:00:22Z hmueller $
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import de.euhm.jlt.dao.TimesWork;
import de.euhm.jlt.utils.AlarmUtils;

/**
 * The manifest Receiver is used to do stuff after device booted. We just
 * re-register the alarms.
 * 
 * Since Android 3.1+ you don't receive BOOT_COMPLETE if user never started the
 * app at least once or user "forced close" application. This was done to
 * prevent malware from automatically registering this service. This security
 * hole was closed in newer versions of Android.
 * 
 * To test BOOT_COMPLETED without restart emulator or real device try this:
 * > adb -s device-id shell am broadcast -a android.intent.action.BOOT_COMPLETED
 * How to get device id? Get list of connected devices with id's:
 * > ...\adt\sdk\platform-tools\adb devices
 */
public class OnBootReceiver extends BroadcastReceiver {
	private final String LOG_TAG = OnBootReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		TimesWork timesWork = new TimesWork(context);
		if (timesWork.getWorkStarted()) {
			Log.v(LOG_TAG, "Re-register JobLogTimer alarms.");
			AlarmUtils.setAlarms(context, timesWork);
		}
	}
}