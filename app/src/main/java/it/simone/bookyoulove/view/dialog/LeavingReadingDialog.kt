package it.simone.bookyoulove.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import it.simone.bookyoulove.R

class LeavingReadingDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val newDialog = activity?.let {

            val builder = AlertDialog.Builder(it)
            builder.setTitle(getString(R.string.leaving_dialog_title))

            builder.setNegativeButton(getString(R.string.delete_string)) { dialog, _ ->
                setFragmentResult("leavingKey", bundleOf("moveToTbr" to false))
                dialog.cancel()
            }

            builder.setPositiveButton(getString(R.string.move_to_tbr_string)) {dialog , _ ->
                setFragmentResult("leavingKey", bundleOf("moveToTbr" to true))
                dialog.cancel()
            }

            builder.create()
        }

        if (newDialog != null) return newDialog
        else throw IllegalStateException("Activity Cannot Be Null")
    }
}