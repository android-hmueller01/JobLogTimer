/*
 * @name JobLogDbHelper.java
 * @author hmueller
 *
 * based on http://developer.android.com/training/basics/data-storage/databases.html
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.euhm.jlt.database.JobLogContract.JobLogTimes;
import de.euhm.jlt.utils.FileUtils;

/**
 * Defines the schema of the database
 * @author hmueller
 */
public class JobLogDbHelper extends SQLiteOpenHelper {
    private final String LOG_TAG = JobLogDbHelper.class.getSimpleName();
	private static final String SQL_TABLE_CREATE = "CREATE TABLE "
			+ JobLogTimes.TABLE_NAME + " (`"
			+ JobLogTimes._ID + "` INTEGER PRIMARY KEY, `"
			+ JobLogTimes.COLUMN_NAME_TIME_START + "` INTEGER, `"
			+ JobLogTimes.COLUMN_NAME_TIME_END + "` INTEGER, `"
			+ JobLogTimes.COLUMN_NAME_HOME_OFFICE + "` BOOLEAN)";
	private static final String SQL_TABLE_DELETE = "DROP TABLE IF EXISTS "
			+ JobLogTimes.TABLE_NAME;
	private static final String SQL_TABLE_UPGRADE_1 = "ALTER TABLE "
			+ JobLogTimes.TABLE_NAME
			+ " ADD `" + JobLogTimes.COLUMN_NAME_HOME_OFFICE + "` BOOLEAN DEFAULT 0";
	public final String DB_FILEPATH;// = "/data/data/{package_name}/databases/database.db";

	public JobLogDbHelper(Context context) {
		super(context, JobLogContract.DATABASE_NAME, null,
				JobLogContract.DATABASE_VERSION);
		DB_FILEPATH = context.getDatabasePath(JobLogContract.DATABASE_NAME).getAbsolutePath();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_TABLE_CREATE);
		Log.d(LOG_TAG, "Database table " + JobLogTimes.TABLE_NAME + " created.");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Upgrade database from version to version
		if ((oldVersion == 1) && (newVersion > oldVersion)) {
			db.execSQL(SQL_TABLE_UPGRADE_1);
			Log.d(LOG_TAG, "Database table " + JobLogTimes.TABLE_NAME + " upgraded from v1 to v2.");
			oldVersion++;
		}
		if ((oldVersion == 2) && (newVersion > oldVersion)) {
			//db.execSQL(SQL_TABLE_UPGRADE_2);
			Log.d(LOG_TAG, "Database table " + JobLogTimes.TABLE_NAME + " upgraded from v2 to v3.");
			oldVersion++;
		}
	}

	/**
	 * Copies the database file at the specified location over the current
	 * internal application database.
	 */
	public boolean importDatabase(String dbPath) throws IOException {
		// Close the SQLiteOpenHelper so it will commit the database to internal storage.
		close();
		File newDb = new File(dbPath);
		File oldDb = new File(DB_FILEPATH);
		if (newDb.exists()) {
			FileUtils.copyFile(new FileInputStream(newDb),
					new FileOutputStream(oldDb));
			// Access the copied database so SQLiteHelper will cache it and mark
			// it as created.
			getWritableDatabase().close();
			return true;
		}
		return false;
	}
	
	/**
	 * Copies the current internal database file to the specified location.
	 */
	public boolean exportDatabase(String dbPath) throws IOException {
		// Close the SQLiteOpenHelper so it will commit the database to internal storage.
		close();
		File newDb = new File(dbPath);
		File oldDb = new File(DB_FILEPATH);
		if (oldDb.exists()) {
			FileInputStream fromFile = new FileInputStream(oldDb);
			// make dir hierarchy, otherwise FileOutputStream() will crash
			File directory = newDb.getParentFile(); // get path without filename
			if (directory != null && !directory.exists()) {
				if (!directory.mkdirs())
					return false;
			}
			FileOutputStream toFile = new FileOutputStream(newDb);
			FileUtils.copyFile(fromFile, toFile);
			return true;
		}
		return false;
	}
}