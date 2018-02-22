/*
 * @file TimesDataSource.java
 * 
 * based on http://www.vogella.com/tutorials/AndroidSQLite/article.html#databasetutorial
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.euhm.jlt.database.JobLogContract.JobLogTimes;
import de.euhm.jlt.database.JobLogDbHelper;

/**
 * This is the DAO (Data Access Object).
 * It maintains the database connection, supports adding new entries and fetching, updating, deleting data.
 * @author hmueller
 */
public class TimesDataSource {
	private final String LOG_TAG = TimesDataSource.class.getSimpleName();
	// Database fields
	private SQLiteDatabase database = null;
	private JobLogDbHelper dbHelper;
	// projection to all columns from the database
	private String[] allColumns = { JobLogTimes._ID,
			JobLogTimes.COLUMN_NAME_TIME_START,
			JobLogTimes.COLUMN_NAME_TIME_END };

	public TimesDataSource(Context context) {
		dbHelper = new JobLogDbHelper(context);
		database = null;
	}

	/**
	 * @return the dbHelper
	 */
	public JobLogDbHelper getDbHelper() {
		return dbHelper;
	}

	/**
	 * Open database, if not already opened
	 * @throws SQLException if the database cannot be opened for writing
	 */
	public void open() throws SQLException {
		// do not reopen, if database already exists
		if (database == null) {
			database = dbHelper.getWritableDatabase();
		}
	}

	/**
	 * Close database.
	 */
	public void close() {
		dbHelper.close();
		database = null; // avoid leaking, and remember that database is closed
	}

	/**
	 * Check if database is already open.
	 * @return Returns true if open.
	 */
	public boolean isOpen() {
		return (database != null);
	}

	public Times createTimes(long timeStart, long timeEnd) {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(JobLogTimes.COLUMN_NAME_TIME_START, timeStart);
		values.put(JobLogTimes.COLUMN_NAME_TIME_END, timeEnd);
		// Insert the new row, returning the primary key value of the new row
		long id = database.insert(JobLogTimes.TABLE_NAME, null, values);
		// read the data back
		return getTimes(id);
	}

	public int updateTimes(long id, long timeStart, long timeEnd) {
		String selection = JobLogTimes._ID + "=" + id;
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(JobLogTimes.COLUMN_NAME_TIME_START, timeStart);
		values.put(JobLogTimes.COLUMN_NAME_TIME_END, timeEnd);
		// Update the row(s), returning the number of rows affected
		int rows = database.update(JobLogTimes.TABLE_NAME, values, selection,
				null);
		return rows;
	}

	public int updateTimes(Times times) {
		return updateTimes(times.getId(), times.getTimeStart(), times.getTimeEnd());
	}

	public Times getTimes(long id) {
		String selection = JobLogTimes._ID + "=" + id;
		Cursor cursor = database.query(JobLogTimes.TABLE_NAME, allColumns,
				selection, null, null, null, null);
		cursor.moveToFirst();
		Times times = cursorToTimes(cursor);
		// make sure to close the cursor
		cursor.close();
		return times;
	}

	public List<Times> getTimeRangeTimes(long timeStart, long timeEnd, String sort) {
		List<Times> list = new ArrayList<>();
		String selection = JobLogTimes.COLUMN_NAME_TIME_START + " BETWEEN " + timeStart + " AND " + timeEnd;
		String orderBy = JobLogTimes.COLUMN_NAME_TIME_START + " " + sort;

		Cursor cursor = database.query(JobLogTimes.TABLE_NAME, allColumns,
				selection, null, null, null, orderBy);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Times times = cursorToTimes(cursor);
			list.add(times);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return list;
	}

	public List<Times> getTimeRangeTimes(long timeStart, long timeEnd) {
		//return getTimeRangeTimes(timeStart, timeEnd, "ASC");
		return getTimeRangeTimes(timeStart, timeEnd, "DESC");
	}

	public List<Times> getAllTimes(String sort) {
		List<Times> list = new ArrayList<>();
		String orderBy = JobLogTimes.COLUMN_NAME_TIME_START + " " + sort;

		Cursor cursor = database.query(JobLogTimes.TABLE_NAME, allColumns,
				null, null, null, null, orderBy);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Times times = cursorToTimes(cursor);
			list.add(times);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return list;
	}


	public List<Times> getAllTimes() {
		return getAllTimes("DESC");
	}

	public int deleteTimes(long id) {
		Log.d(LOG_TAG, "Times deleted with id=" + id);
		String selection = JobLogTimes._ID + "=" + id;
		int rows = database.delete(JobLogTimes.TABLE_NAME, selection, null);
		return rows;
	}

	public int deleteTimes(Times times) {
		long id = times.getId();
		return deleteTimes(id);
	}

	private Times cursorToTimes(Cursor cursor) {
		Times times = new Times(cursor.getLong(0), cursor.getLong(1), cursor.getLong(2));
		/* obsolete since new Times constructor
		times.setId(cursor.getLong(0));
		times.setTimeStart(cursor.getLong(1));
		times.setTimeEnd(cursor.getLong(2));
		*/
		return times;
	}
}