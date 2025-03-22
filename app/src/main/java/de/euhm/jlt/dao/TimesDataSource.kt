/*
 * @file TimesDataSource.kt
 * @author Holger Mueller
 * 
 * based on http://www.vogella.com/tutorials/AndroidSQLite/article.html#databasetutorial
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import de.euhm.jlt.database.JobLogContract.JobLogTimes
import de.euhm.jlt.database.JobLogDbHelper

/**
 * This is the DAO (Data Access Object).
 * It maintains the database connection, supports adding new entries and fetching, updating, deleting data.
 * It will automatically open the database connection. Please use `close()` to close the connection.
 *
 * @author hmueller
 *
 * @param context Context
 */
class TimesDataSource(context: Context?) {
    @Suppress("PrivatePropertyName")
    private val LOG_TAG: String = TimesDataSource::class.java.simpleName

    /**
     * @return The dbHelper
     */
    val dbHelper: JobLogDbHelper = JobLogDbHelper(context)

    // Database fields
    private var mDatabase: SQLiteDatabase = dbHelper.writableDatabase

    // projection to all columns from the database
    private val allColumns = arrayOf(JobLogTimes._ID,
        JobLogTimes.COLUMN_NAME_TIME_START,
        JobLogTimes.COLUMN_NAME_TIME_END,
        JobLogTimes.COLUMN_NAME_HOME_OFFICE)

    /**
     * Open database, if not already opened
     * @throws SQLException if the database cannot be opened for writing
     */
    @Throws(SQLException::class)
    fun open() {
        // do not reopen, if database already exists
        if (!isOpen) {
            mDatabase = dbHelper.writableDatabase
        }
    }

    /**
     * Close database.
     */
    fun close() {
        mDatabase.close()
    }

    /**
     * Check if database is already open.
     * @return Returns true if open.
     */
    val isOpen: Boolean
        get() = (mDatabase.isOpen)

    private fun createTimes(timeStart: Long, timeEnd: Long, homeOffice: Boolean): Times {
        open()
        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(JobLogTimes.COLUMN_NAME_TIME_START, timeStart)
        values.put(JobLogTimes.COLUMN_NAME_TIME_END, timeEnd)
        values.put(JobLogTimes.COLUMN_NAME_HOME_OFFICE, homeOffice)
        // Insert the new row, returning the primary key value of the new row
        val id = mDatabase.insert(JobLogTimes.TABLE_NAME, null, values)
        // read the data back
        return getTimes(id)
    }

    fun createTimes(times: Times): Times {
        return createTimes(times.timeStart, times.timeEnd, times.homeOffice)
    }

    fun createTimes(tw: TimesWork): Times {
        return createTimes(tw.timeStart, tw.timeEnd, tw.homeOffice)
    }

    private fun updateTimes(id: Long, timeStart: Long, timeEnd: Long, homeOffice: Boolean): Int {
        open()
        val selection = JobLogTimes._ID + "=" + id
        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(JobLogTimes.COLUMN_NAME_TIME_START, timeStart)
        values.put(JobLogTimes.COLUMN_NAME_TIME_END, timeEnd)
        values.put(JobLogTimes.COLUMN_NAME_HOME_OFFICE, homeOffice)
        // Update the row(s), returning the number of rows affected
        val rows = mDatabase.update(JobLogTimes.TABLE_NAME, values, selection, null)
        return rows
    }

    fun updateTimes(times: Times): Int {
        return updateTimes(times.id, times.timeStart, times.timeEnd, times.homeOffice)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getTimes(id: Long): Times {
        open()
        val selection = JobLogTimes._ID + "=" + id
        val cursor = mDatabase.query(JobLogTimes.TABLE_NAME, allColumns, selection, null, null, null, null)
        cursor.moveToFirst()
        val times = cursorToTimes(cursor)
        // make sure to close the cursor
        cursor.close()
        return times
    }

    fun getTimeRangeTimes(timeStart: Long, timeEnd: Long, sort: String): List<Times> {
        open()
        val list: MutableList<Times> = ArrayList()
        val selection = JobLogTimes.COLUMN_NAME_TIME_START + " BETWEEN " + timeStart + " AND " + timeEnd
        val orderBy = JobLogTimes.COLUMN_NAME_TIME_START + " " + sort

        val cursor = mDatabase.query(JobLogTimes.TABLE_NAME, allColumns, selection, null, null, null, orderBy)

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val times = cursorToTimes(cursor)
            list.add(times)
            cursor.moveToNext()
        }
        // make sure to close the cursor
        cursor.close()
        return list
    }

    fun getTimeRangeTimes(timeStart: Long, timeEnd: Long): List<Times> {
        //return getTimeRangeTimes(timeStart, timeEnd, "ASC");
        return getTimeRangeTimes(timeStart, timeEnd, "DESC")
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getAllTimes(sort: String): List<Times> {
        open()
        val list: MutableList<Times> = ArrayList()
        val orderBy = JobLogTimes.COLUMN_NAME_TIME_START + " " + sort

        val cursor = mDatabase.query(JobLogTimes.TABLE_NAME, allColumns, null, null, null, null, orderBy)

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val times = cursorToTimes(cursor)
            list.add(times)
            cursor.moveToNext()
        }
        // make sure to close the cursor
        cursor.close()
        return list
    }


    val allTimes: List<Times>
        get() = getAllTimes("DESC")

    @Suppress("MemberVisibilityCanBePrivate")
    fun deleteTimes(id: Long): Int {
        Log.d(LOG_TAG, "Times deleted with id=$id")
        open()
        val selection = JobLogTimes._ID + "=" + id
        val rows = mDatabase.delete(JobLogTimes.TABLE_NAME, selection, null)
        return rows
    }

    fun deleteTimes(times: Times): Int {
        val id = times.id
        return deleteTimes(id)
    }

    private fun cursorToTimes(cursor: Cursor): Times {
        val times = Times(cursor.getLong(0), cursor.getLong(1), cursor.getLong(2), cursor.getInt(3) != 0)
        return times
    }
}
