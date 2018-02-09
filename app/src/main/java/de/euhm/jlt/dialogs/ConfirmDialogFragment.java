/**
 * $Id: ConfirmDialogFragment.java 184 2016-12-21 21:32:19Z hmueller $
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Implements a DialogFragment for confirming
 * @author hmueller
 * @version $Rev: 184 $
 */
public class ConfirmDialogFragment extends DialogFragment {
	public interface YesNoListener {
		void onConfirmDialogYes();

		void onConfirmDialogNo();
	}

	private static int mTitleId;
	private static CharSequence mMessage;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof YesNoListener)) {
			throw new ClassCastException(activity.toString()
					+ " must implement YesNoListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
		alertDialog.setMessage(mMessage);
		alertDialog.setPositiveButton(android.R.string.yes, // R.string.button_ok
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								((YesNoListener) getActivity()).onConfirmDialogYes();
							}
						});
		alertDialog.setNegativeButton(android.R.string.no, // R.string.button_cancel
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								((YesNoListener) getActivity()).onConfirmDialogNo();
							}
						});

		setCancelable(false);
		if (mTitleId != 0) alertDialog.setTitle(mTitleId);
		return alertDialog.create();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//  clean up stored references to avoid leaking
	}

	// set internal title id
	public void setTitel(int titleId) {
		mTitleId = titleId;
	}

	// set internal message
	public void setMessage(CharSequence message) {
		mMessage = message;
	}

}
