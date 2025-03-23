/**
 * @file OnBootReceiver.kt
 *
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import de.euhm.jlt.dao.TimesWork
import de.euhm.jlt.utils.AlarmUtils

/**
 * The manifest Receiver is used to do stuff after device booted. We just
 * re-register the alarms.
 *
 * Since Android 3.1+ you don't receive BOOT_COMPLETE if user never started the
 * app at least once or user "forced close" application. This was done to
 * prevent malware from automatically registering this service. This security
 * hole was closed in newer versions of Android.
 *
 * To test BOOT_COMPLETED without restart emulator or real device try this:
 * > adb -s device-id shell am broadcast -a android.intent.action.BOOT_COMPLETED
 * How to get device id? Get list of connected devices with id's:
 * > ...\adt\sdk\platform-tools\adb devices
 */
class OnBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // test if the received intent is really a BOOT_COMPLETED
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            if (TimesWork.workStarted) {
                Log.v(LOG_TAG, "Re-register JobLogTimer alarms.")
                AlarmUtils.setAlarms(context)
            }
        } else {
            Log.e(LOG_TAG, "getAction() != ACTION_BOOT_COMPLETED. This should never happen!")
        }
    }

    companion object {
        private val LOG_TAG: String by lazy { OnBootReceiver::class.java.simpleName }
    }
}