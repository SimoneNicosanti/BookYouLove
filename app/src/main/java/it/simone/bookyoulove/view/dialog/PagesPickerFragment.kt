package it.simone.bookyoulove.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.app.Instrumentation
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentPagesPickerBinding
import it.simone.bookyoulove.view.MAX_PAGES_AMOUNT
import it.simone.bookyoulove.view.MIN_PAGES_AMOUNT
import it.simone.bookyoulove.viewmodel.NewReadingBookViewModel
import java.lang.IllegalStateException


class PagesPickerFragment : DialogFragment() {

    private lateinit var binding: FragmentPagesPickerBinding

    private val newReadingBookViewModel: NewReadingBookViewModel by viewModels({requireParentFragment()})

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val newDialog = activity?.let {
            val inflater = requireActivity().layoutInflater
            binding = FragmentPagesPickerBinding.inflate(inflater)

            binding.pagesPickerInput.maxValue = MAX_PAGES_AMOUNT
            binding.pagesPickerInput.minValue = MIN_PAGES_AMOUNT

            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.pages_picker_title)
            builder.setView(binding.root)

            builder.setPositiveButton(R.string.confirm_string,
                DialogInterface.OnClickListener { dialog, _ ->
                    val settedPages = binding.pagesPickerInput.value
                    setFragmentResult("pagesKey", bundleOf("settedPages" to settedPages))
                    dialog.cancel()
                })

            builder.setNegativeButton(R.string.back_string,
                DialogInterface.OnClickListener { dialog, _ ->
                    dialog.cancel()
                })

            builder.create()
        }

        if (newDialog != null) return newDialog
        else throw IllegalStateException("Activity Cannot be Null")
    }



}