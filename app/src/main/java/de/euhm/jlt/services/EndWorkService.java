/**
 * $Id: $
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
import de.euhm.jlt.dao.Times;
import de.euhm.jlt.dao.TimesDataSource;
import de.euhm.jlt.dao.TimesWork;
import de.euhm.jlt.utils.AlarmUtils;
import de.euhm.jlt.utils.Constants;
import de.euhm.jlt.utils.TimeUtil;

/**
 * Service to end work
 * @author hmueller
 */
public class EndWorkService extends Service {
	private final String LOG_TAG = EndWorkService.class.getSimpleName();
	
	@Override
	public void onCreate() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		Log.v(LOG_TAG, "Received EndWorkService");
		// Construct/load TimesWork DAO from persistent data
		TimesWork timesWork = new TimesWork(this);
		if (!timesWork.getWorkStarted()) {
			// this should never happen, log and break here
			Log.e(LOG_TAG, "Got an ACTION_END_WORK action while work was already stopped!");
		} else {
			// End work ...
			if (timesWork.getTimeEnd() == -1) {
				// only use current time, if not set manually
				timesWork.setTimeEnd(TimeUtil.getCurrentTimeInMillis());
			}
			timesWork.setWorkStarted(false);

			// write data to database
			Times times = new Times(0, timesWork.getTimeStart(), timesWork.getTimeEnd());
			TimesDataSource mDatasource = new TimesDataSource(this);
		    mDatasource.open();
			times = mDatasource.createTimes(times.getTimeStart(), times.getTimeEnd());
		    mDatasource.close();

			// Store TimesWork DAO to persistent data
			timesWork.saveTimesWork();

			// cancel alarms and notification
			AlarmUtils.setAlarms(this, timesWork);
	        
        	// update views that changes take place
        	sendBroadcast(new Intent(Constants.RECEIVER_UPDATE_VIEW));
        	
        	// update AppWidgetProvider
        	sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE));

    		Toast.makeText(this, R.string.work_ended, Toast.LENGTH_SHORT).show();
	    }

        // do not restart if service is killed
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
