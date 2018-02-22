/**
 * @file CustomApplication.java
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt;

import android.app.Application;

/**
 * Custom application to keep track of the activity visibility
 * @author hmueller
 */
public class CustomApplication extends Application {

	  private static boolean mActivityVisible;
	  
	  public static boolean isActivityVisible() {
	    return mActivityVisible;
	  }  

	  public static void activityResumed() {
	    mActivityVisible = true;
	  }

	  public static void activityPaused() {
	    mActivityVisible = false;
	  }
}
