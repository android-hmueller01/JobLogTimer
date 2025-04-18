/**
 * @file AboutFragment.kt
 *
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.BackEventCompat
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment

/**
 * Fragment for showing the about screen
 * @author hmueller
 */
class AboutFragment : DialogFragment() {

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val builder = AlertDialog.Builder(activity)
        // Get the layout inflater
        val inflater = activity.layoutInflater
        // Inflate, pass null as the parent view because its going in the dialog layout
        val view = inflater.inflate(R.layout.fragment_about, null)

        // set version name defined in Manifest
        val pm = activity.packageManager
        val packageName = activity.packageName
        var versionName = "unknown"
        try {
            versionName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)).versionName!!
            } else {
                pm.getPackageInfo(packageName, 0).versionName!!
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("AboutFragment", "NameNotFoundException: $e")
        }
        val version = view.findViewById<TextView>(R.id.about_version_val)
        version.text = versionName

        // set the layout for the dialog
        builder.setView(view)
        isCancelable = true
        val dialog = builder.create()

        // TODO
        // requireActivity().onBackPressedDispatcher.addCallback(...) works for fragments attached to an activity,
        // but a DialogFragment shows as a dialog (not part of the activity's back stack) and handles
        // the back press internally, often via the Dialog itself.
        // So onBackPressedDispatcher won't be triggered when the back button is pressed if your DialogFragment
        // is still showing as a dialog — because the dialog consumes the back press first.

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Your custom back navigation logic here
                // You can use the FragmentManager to navigate within the fragment
                // or call requireActivity().finish() to close the activity

                // Example: Pop the back stack if there's anything to pop
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    // Otherwise, let the activity handle it (e.g., close the activity)
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                // Example: dismiss the dialog
                // dismiss()
            }

            override fun handleOnBackStarted(backEvent: BackEventCompat) {
                // Optional: Handle the start of the back gesture in fragment
                Log.v("AboutFragment", "handleOnBackStarted()")
                super.handleOnBackStarted(backEvent)
            }
        })

        return dialog
    }
}
