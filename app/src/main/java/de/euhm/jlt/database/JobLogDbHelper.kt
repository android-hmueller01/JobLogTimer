/**
 * @name JobLogDbHelper.kt
 *
 * based on http://developer.android.com/training/basics/data-storage/databases.html
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import de.euhm.jlt.database.JobLogContract.JobLogTimes
import de.euhm.jlt.utils.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

private val LOG_TAG: String = JobLogDbHelper::class.java.simpleName

/**
 * Defines the schema of the database
 * @author hmueller
 */
class JobLogDbHelper(context: Context) :
    SQLiteOpenHelper(context, JobLogContract.DATABASE_NAME, null, JobLogContract.DATABASE_VERSION) {
    private val dbFilePath: String =
        context.getDatabasePath(JobLogContract.DATABASE_NAME).absolutePath // = "/data/data/{package_name}/databases/database.db";

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_TABLE_CREATE)
        Log.d(LOG_TAG, "Database table " + JobLogTimes.TABLE_NAME + " created.")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Upgrade database from version to version
        var oldVer = oldVersion
        if ((oldVer == 1) && (newVersion > oldVer)) {
            db.execSQL(SQL_TABLE_UPGRADE_1)
            Log.d(LOG_TAG, "Database table " + JobLogTimes.TABLE_NAME + " upgraded from v1 to v2.")
            oldVer++
        }
        if ((oldVer == 2) && (newVersion > oldVer)) {
            //db.execSQL(SQL_TABLE_UPGRADE_2);
            Log.d(LOG_TAG, "Database table " + JobLogTimes.TABLE_NAME + " upgraded from v2 to v3.")
            //oldVer++
        }
    }

    /**
     * Copies the database file at the specified location over the current
     * internal application database.
     */
    @Throws(IOException::class)
    fun importDatabase(dbPath: String): Boolean {
        // Close the SQLiteOpenHelper so it will commit the database to internal storage.
        close()
        val newDb = File(dbPath)
        val oldDb = File(dbFilePath)
        if (newDb.exists()) {
            FileUtils.copyFile(FileInputStream(newDb), FileOutputStream(oldDb))
            // Access the copied database so SQLiteHelper will cache it and mark
            // it as created.
            writableDatabase.close()
            return true
        }
        return false
    }

    /**
     * Copies the current internal database file to the specified location.
     */
    @Throws(IOException::class)
    fun exportDatabase(dbPath: String): Boolean {
        // Close the SQLiteOpenHelper so it will commit the database to internal storage.
        close()
        val newDb = File(dbPath)
        val oldDb = File(dbFilePath)
        if (oldDb.exists()) {
            val fromFile = FileInputStream(oldDb)
            // make dir hierarchy, otherwise FileOutputStream() will crash
            val directory = newDb.parentFile // get path without filename
            if (directory != null && !directory.exists()) {
                if (!directory.mkdirs()) return false
            }
            val toFile = FileOutputStream(newDb)
            FileUtils.copyFile(fromFile, toFile)
            return true
        }
        return false
    }

    internal companion object {
        private const val SQL_TABLE_CREATE = ("CREATE TABLE " + JobLogTimes.TABLE_NAME + " (`" + //
                JobLogTimes._ID + "` INTEGER PRIMARY KEY, `" + //
                JobLogTimes.COLUMN_NAME_TIME_START + "` INTEGER, `" + //
                JobLogTimes.COLUMN_NAME_TIME_END + "` INTEGER, `" + //
                JobLogTimes.COLUMN_NAME_HOME_OFFICE + "` BOOLEAN)")
        //private const val SQL_TABLE_DELETE = ("DROP TABLE IF EXISTS " + JobLogTimes.TABLE_NAME)
        private const val SQL_TABLE_UPGRADE_1 =
            ("ALTER TABLE " + JobLogTimes.TABLE_NAME + " ADD `" + JobLogTimes.COLUMN_NAME_HOME_OFFICE + "` BOOLEAN DEFAULT 0")
    }
}