/**
 * @file SettingsActivity.kt
 *
 * Settings activity of JobLogTimer
 *
 * MIT License
 * Copyright (c) 2025 Holger Mueller
 */
package de.euhm.jlt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import de.euhm.jlt.utils.Prefs

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }

        val supportToolbar: Toolbar? = findViewById(R.id.settings_toolbar)
        setSupportActionBar(supportToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable the back button in the action bar
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // set inverse enable/disable of "pref_break_atfixtime_key"
        val prefs = Prefs(requireActivity())
        findPreference<EditTextPreference>("pref_break_atfixtime_key")?.isEnabled = !prefs.breakAfterHoursEnabled

        // handle inverse enable/disable of "pref_break_atfixtime_key"
        // can not be implemented by an "android:dependency" setting
        findPreference<CheckBoxPreference>("pref_break_after_hours_enable_key")?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is Boolean) {
                preferenceScreen.findPreference<EditTextPreference>("pref_break_atfixtime_key")?.isEnabled =
                    !newValue
            }
            true
        }
    }
}