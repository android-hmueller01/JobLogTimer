/**
 * $Id: StartWorkService.java 90 2015-02-11 19:38:55Z hmueller $
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.services;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import de.euhm.jlt.R;
import de.euhm.jlt.dao.TimesWork;
import de.euhm.jlt.utils.AlarmUtils;
import de.euhm.jlt.utils.Constants;
import de.euhm.jlt.utils.TimeUtil;

/**
 * Service to start work
 * @author hmueller
 */
public class StartWorkService extends Service {
	private final String LOG_TAG = StartWorkService.class.getSimpleName();
	
	@Override
	public void onCreate() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		Log.v(LOG_TAG, "Received StartWorkService");
		// Construct/load TimesWork DAO from persistent data
		TimesWork timesWork = new TimesWork(this);
		if (timesWork.getWorkStarted()) {
			// this should never happen, log and break here
			Log.e(LOG_TAG, "Got an ACTION_START_WORK action while work was already started!");
		} else {
			// Start work ...
			timesWork.setTimeStart(TimeUtil.getCurrentTimeInMillis());
			timesWork.setTimeEnd(-1);
			timesWork.setWorkStarted(true);

			// Store TimesWork DAO to persistent data
			timesWork.saveTimesWork();

			// set alarms and notification
			AlarmUtils.setAlarms(this, timesWork);

        	// update views that changes take place
        	sendBroadcast(new Intent(Constants.RECEIVER_UPDATE_VIEW));
        	
        	// update AppWidgetProvider
			sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE));

    		Toast.makeText(this, R.string.work_started, Toast.LENGTH_SHORT).show();
	    }

        // do not restart if service is killed
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
