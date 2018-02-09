/**
 * $Id: JobLogContract.java 40 2015-01-20 19:59:38Z hmueller $
 * 
 * based on http://developer.android.com/training/basics/data-storage/databases.html
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.database;

import android.provider.BaseColumns;

/**
 * Defines the schema of the database
 * @author hmueller
 * @version $Rev: 40 $
 */
public final class JobLogContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public JobLogContract() {}

	// If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "JobLogTimer.db";

    /* Inner class that defines the table contents */
    public static abstract class JobLogTimes implements BaseColumns {
        public static final String TABLE_NAME = "times";
        public static final String COLUMN_NAME_TIME_START = "start";
        public static final String COLUMN_NAME_TIME_END = "end";
        public static final String COLUMN_NAME_ = "subtitle";
    }
}
