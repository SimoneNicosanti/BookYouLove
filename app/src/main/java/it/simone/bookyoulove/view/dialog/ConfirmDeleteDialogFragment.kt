package it.simone.bookyoulove.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import it.simone.bookyoulove.R

class ConfirmDeleteDialogFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = activity?.let {

            val builder = AlertDialog.Builder(it)
            builder.setTitle(arguments?.getString("itemToDelete"))

            builder.setMessage(arguments?.getString("deleteMessageKey", null))

            builder.setPositiveButton(R.string.confirm_string) { dialog, _ ->
                setFragmentResult("deleteKey", bundleOf("deleteConfirm" to true))
                dialog.cancel()
            }

            builder.setNegativeButton(R.string.back_string) { dialog, _ ->
                dialog.cancel()
            }

            builder.create()
        }

        return dialog ?: throw IllegalStateException()
    }
}