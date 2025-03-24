/**
 * @file ConfirmDialogFragment.kt
 *
 * Converted from Java to Kotlin by Android Studio Meerkat | 2024.3.1 in March 2025
 *
 * MIT License
 * Copyright (c) 2014-2025 Holger Mueller
 */
package de.euhm.jlt.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment

/**
 * Implements a DialogFragment for confirming
 * @author hmueller
 */
class ConfirmDialogFragment : DialogFragment() {
    interface YesNoListener {
        fun onConfirmDialogYes()

        fun onConfirmDialogNo()
    }

    private var mTitleId = 0
    private var mMessage: CharSequence? = null
    private lateinit var mListener: YesNoListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is YesNoListener) {
            mListener = context // Now it's safe to cast
        } else {
            throw ClassCastException("$context must implement YesNoListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialog = AlertDialog.Builder(activity)
        alertDialog.setMessage(mMessage)
        alertDialog.setPositiveButton(android.R.string.ok) { _, _ -> mListener.onConfirmDialogYes() }
        alertDialog.setNegativeButton(android.R.string.cancel) { _, _ -> mListener.onConfirmDialogNo() }

        isCancelable = false
        if (mTitleId != 0) alertDialog.setTitle(mTitleId)
        return alertDialog.create()
    }

    // set internal title id
    @Suppress("unused")
    fun setTitel(titleId: Int) {
        mTitleId = titleId
    }

    // set internal message
    fun setMessage(message: CharSequence) {
        mMessage = message
    }
}
