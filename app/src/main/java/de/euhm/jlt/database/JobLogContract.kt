/**
 * @name JobLogContract.kt
 * 
 * based on http://developer.android.com/training/basics/data-storage/databases.html
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.database

import android.provider.BaseColumns

/**
 * Defines the schema of the database
 * @author hmueller
 */
object JobLogContract {
    // If you change the database schema, you must increment the database version.
    const val DATABASE_VERSION: Int = 2
    const val DATABASE_NAME: String = "JobLogTimer.db"

    /* Inner class that defines the table contents */
    object JobLogTimes : BaseColumns {
        @Suppress("ConstPropertyName")
        const val _ID: String = BaseColumns._ID
        const val TABLE_NAME: String = "times"
        const val COLUMN_NAME_TIME_START: String = "start"
        const val COLUMN_NAME_TIME_END: String = "end"
        const val COLUMN_NAME_HOME_OFFICE: String = "h_office"
    }
}
