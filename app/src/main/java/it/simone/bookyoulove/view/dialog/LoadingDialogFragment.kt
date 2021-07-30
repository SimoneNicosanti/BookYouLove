package it.simone.bookyoulove.view.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentLoadingDialogFragmentBinding
import it.simone.bookyoulove.viewmodel.*


class LoadingDialogFragment : DialogFragment() {

    private lateinit var binding : FragmentLoadingDialogFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        // Make Dialog Incancellable: This dialog is used in order to wait a long time operation
        isCancelable = false
        binding = FragmentLoadingDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

}