/**
 * @file ConfirmDialogFragment.java
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

/**
 * Implements a DialogFragment for confirming
 * @author hmueller
 */
public class ConfirmDialogFragment extends DialogFragment {
	public interface YesNoListener {
		void onConfirmDialogYes();

		void onConfirmDialogNo();
	}

	private static int mTitleId;
	private static CharSequence mMessage;
	private YesNoListener mListener;

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if ((context instanceof YesNoListener)) {
			mListener = (YesNoListener) context; // Now it's safe to cast
		} else {
			throw new ClassCastException(context
					+ " must implement YesNoListener");
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
		alertDialog.setMessage(mMessage);
		alertDialog.setPositiveButton(android.R.string.yes, // R.string.button_ok
				(dialog, which) -> mListener.onConfirmDialogYes());
		alertDialog.setNegativeButton(android.R.string.no, // R.string.button_cancel
				(dialog, which) -> mListener.onConfirmDialogNo());

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
