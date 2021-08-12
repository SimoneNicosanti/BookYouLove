package it.simone.bookyoulove.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import it.simone.bookyoulove.R
import java.lang.IllegalStateException

class ConfirmDeleteDialogFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = activity?.let {

            val builder = AlertDialog.Builder(it)
            builder.setTitle(String.format(resources.getString(R.string.alert_delete_string, arguments?.getString("itemToDelete"))))

            builder.setPositiveButton(R.string.confirm_string ,
            DialogInterface.OnClickListener { dialog, which ->
                setFragmentResult("deleteKey", bundleOf("deleteConfirm" to true))
                dialog.cancel()
            })

            builder.setNegativeButton(R.string.back_string ,
            DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
            })

            builder.create()
        }

        return dialog ?: throw IllegalStateException()
    }
}