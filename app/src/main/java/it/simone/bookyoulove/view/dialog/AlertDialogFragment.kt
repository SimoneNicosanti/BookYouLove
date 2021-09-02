package it.simone.bookyoulove.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import it.simone.bookyoulove.R

class AlertDialogFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = activity?.let {

            val builder = AlertDialog.Builder(it)
            builder.setTitle(arguments?.getString("alertDialogTitleKey"))

            builder.setPositiveButton(R.string.confirm_string) { dialog, _ ->
                dialog.cancel()
            }
            builder.create()
        }

        return dialog ?: throw IllegalStateException()
    }
}