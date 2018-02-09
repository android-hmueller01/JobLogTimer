/**
 * $Id: SettingsSectionFragment.java 184 2016-12-21 21:32:19Z hmueller $
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import de.euhm.jlt.preferences.PreferenceFragment;
import de.euhm.jlt.preferences.Prefs;

/**
 * Settings/Preference section fragment of JobLog
 * 
 * @author hmueller
 * @version $Rev: 184 $
 */
public class SettingsSectionFragment extends PreferenceFragment {

	@Override
	/*
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	*/
	public void onAttach(Context context) {
	    super.onAttach(context);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setHasOptionsMenu(true);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
        // set inverse enable/disable of "pref_break_atfixtime_key"
		Prefs prefs = new Prefs(getActivity());
        findPreference("pref_break_atfixtime_key")
                    	.setEnabled(!prefs.getBreakAfterHoursEnabled());
        // handle inverse enable/disable of "pref_break_atfixtime_key"
        // can not be implemented by an "android:dependency" setting
        findPreference("pref_break_after_hours_enable_key")
        	.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                if (newValue instanceof Boolean) {
                    Boolean isEnabled = (Boolean)newValue;
                    getPreferenceScreen()
                    	.findPreference("pref_break_atfixtime_key")
                    	.setEnabled(!isEnabled);
                }
                return true;
            }
        });
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//  clean up stored references to avoid leaking
	}

}
