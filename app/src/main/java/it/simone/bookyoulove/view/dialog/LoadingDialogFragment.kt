package it.simone.bookyoulove.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import it.simone.bookyoulove.databinding.FragmentLoadingDialogFragmentBinding


class LoadingDialogFragment : DialogFragment() {

    private lateinit var binding : FragmentLoadingDialogFragmentBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        // Make Dialog Incancellable: This dialog is used in order to wait a long time operation
        /*
        callback = requireActivity().onBackPressedDispatcher.addCallback {
            requireParentFragment().findNavController().popBackStack()
            dialog?.dismiss()
        }
        callback.isEnabled = true*/

        isCancelable = false
        binding = FragmentLoadingDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

}