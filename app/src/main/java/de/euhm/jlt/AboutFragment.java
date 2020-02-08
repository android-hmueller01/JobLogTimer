/*
 * @file AboutFragment.java
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

/**
 * Fragment for showing the about screen
 * @author hmueller
 */
public class AboutFragment extends DialogFragment {
	
	public AboutFragment() {
		// create an empty constructor! No args allowed!
	}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("InflateParams")
	@Override
	@NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate, pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.fragment_about, null);

        // set version name defined in Manifest
        PackageManager pm = getActivity().getPackageManager();
        String packageName = getActivity().getPackageName();
        String versionName = "unknown";
		try {
			versionName = pm.getPackageInfo(packageName, 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
   		TextView version = view.findViewById(R.id.about_version_val);
        version.setText(versionName);

        // set the layout for the dialog
        builder.setView(view);
        setCancelable(true);

        return builder.create();
    }
}
