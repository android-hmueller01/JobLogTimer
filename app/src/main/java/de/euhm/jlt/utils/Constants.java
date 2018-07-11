/*
 * @file Constants.java
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.utils;

/**
 * Helper class for global constants
 * @author hmueller
 */
public final class Constants {
	// Notification IDs
	public static final int NOTIFICATION_END_WORK = 1;
	public static final String NOTIFICATION_CHANNEL = "WORK DONE";
	
	// Action Codes
	//public static final String ACTION_UPDATE_WIDGET = "de.euhm.jlt.ACTION_UPDATE_WIDGET";
	// use AppWidgetManager.ACTION_APPWIDGET_UPDATE instead

	// Receiver Codes
	public static final String RECEIVER_RECREATE = "de.euhm.jlt.RECREATE";
	public static final String RECEIVER_UPDATE_VIEW = "de.euhm.jlt.UPDATE_VIEW";
	public static final String RECEIVER_NORMAL_WORK_ALARM = "de.euhm.jlt.NORMAL_WORK_ALARM";
	public static final String RECEIVER_MAX_WORK_ALARM = "de.euhm.jlt.MAX_WORK_ALARM";

	// Permission Codes
	public static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
	public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 2;

	// Button IDs
	public static final int BUTTON_OK     = 0x100001;
	public static final int BUTTON_DELETE = 0x100002;
	public static final int BUTTON_CANCEL = 0x100003;
	public static final int BUTTON_CLEAR  = 0x100004;

	// Other constants
	public static final long WIDGET_UPDATE_INTERVAL = 60*1000; // 1 minute
	
	// break times by German law
	public static final long GL_WORK_TIME1 = 6 * 60 * 60 * 1000; // 6 h
	public static final long GL_WORK_TIME2 = 9 * 60 * 60 * 1000; // 9 h
	public static final long GL_BREAK_TIME1 = 30 * 60 * 1000; // 30 min.
	public static final long GL_BREAK_TIME2 = 45 * 60 * 1000; // 45 min.
/*
    public static final class array {
        public static final int bla=0x2;
    }
*/
}
