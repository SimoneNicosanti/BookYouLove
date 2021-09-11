package it.simone.bookyoulove.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentCoverLinkPickerBinding


class CoverLinkPickerFragment : DialogFragment() {

    private lateinit var binding: FragmentCoverLinkPickerBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val newDialog = activity?.let {
            val inflater = requireActivity().layoutInflater
            binding = FragmentCoverLinkPickerBinding.inflate(inflater)

            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.cover_link_picker_msg_sting)
            builder.setView(binding.root)

            builder.setPositiveButton(R.string.confirm_string
            ) { dialog, _ ->
                val settedLink = if (binding.coverLinkPickerInput.text != null) binding.coverLinkPickerInput.text.toString() else ""
                setFragmentResult("coverLinkKey", bundleOf("settedCoverLink" to settedLink))
                dialog?.cancel()
            }

            builder.setNegativeButton(R.string.back_string
            ) { dialog, _ ->
                dialog.cancel()
            }

            builder.create()
        }

        if (newDialog != null) return newDialog
        else throw IllegalStateException("Activity Cannot Be Null")
    }

}