/*
 * @file AlarmWorkMaxService.java
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.services;

import java.io.File;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import de.euhm.jlt.MainActivity;
import de.euhm.jlt.R;
import de.euhm.jlt.utils.Constants;

/**
 * Alarm service for max work time
 * @author hmueller
 */
public class AlarmWorkMaxService extends Service {
	private final String LOG_TAG = AlarmWorkMaxService.class.getSimpleName();
	
	@Override
	public void onCreate() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		Log.v(LOG_TAG, "Received AlarmWorkMaxService");
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent =
            PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent endWorkIntent = new Intent(this, EndWorkService.class);
        PendingIntent endWorkPendingIntent =
            PendingIntent.getService(this, 0, endWorkIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
        	    new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL)
        	    .setSmallIcon(R.drawable.ic_launcher_bw)
        	    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_color))
        	    .setContentTitle(getResources().getText(R.string.app_name))
        	    .setContentText(getResources().getText(R.string.work_time_max_notif_text))
        	    .setTicker(getResources().getText(R.string.work_time_max_ticker))
        	    .setLights(ContextCompat.getColor(this, R.color.alarm_max_work_time_led), 200, 200)
        	    .setSound(Uri.parse("android.resource://" + getPackageName() + File.separator + R.raw.beeps))
				// icon is not shown since Nougat https://stackoverflow.com/questions/41503972/icon-is-not-getting-displayed-in-notification-in-android-nougat
        	    .addAction(R.drawable.ic_action_stop, getResources().getText(R.string.button_end),
        	    		endWorkPendingIntent)
        	    .setContentIntent(resultPendingIntent);
        // Gets an instance of the NotificationManager service
        NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (notifyMgr != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL,
						getString(R.string.notification_channel), NotificationManager.IMPORTANCE_HIGH);
				notifyMgr.createNotificationChannel(channel);
			}
			// Builds the notification and issues it.
			notifyMgr.notify(Constants.NOTIFICATION_END_WORK, builder.build());
		}

        // do not restart if service is killed
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}