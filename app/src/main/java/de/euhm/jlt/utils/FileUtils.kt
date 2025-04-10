/**
 * @file FileUtils.kt
 *
 * Taken from http://stackoverflow.com/questions/6540906/simple-export-and-import-of-a-sqlite-database-on-android
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.utils

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel

/**
 * Helper class to copy files.
 *
 * @author Austyn Mahoney, hmueller
 */
object FileUtils {
    /**
     * Creates the specified `toFile` as a byte for byte copy of the `fromFile`.
     * If `toFile` already exists, then it will be replaced with a copy of `fromFile`.
     * The name and path of `toFile` will be that of `toFile`.
     *
     * Note: `fromFile` and `toFile` will be closed by this function.
     *
     * @author Austyn Mahoney, hmueller
     *
     * @param fromFile FileInputStream for the file to copy from.
     * @param toFile FileInputStream for the file to copy to.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copyFile(fromFile: FileInputStream, toFile: FileOutputStream) {
        val fromChannel: FileChannel = fromFile.channel
        val toChannel: FileChannel = toFile.channel
        try {
            fromChannel.transferTo(0, fromChannel.size(), toChannel)
        } finally {
            toChannel.use {
                fromChannel.close()
            }
            // Java version: try { fromChannel.close() } finally { toChannel.close() }
        }
    }
}