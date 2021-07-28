package it.simone.bookyoulove.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import it.simone.bookyoulove.R
import it.simone.bookyoulove.viewmodel.DetailReadingViewModel
import it.simone.bookyoulove.viewmodel.NewReadingBookViewModel
import it.simone.bookyoulove.viewmodel.EndedViewModel
import it.simone.bookyoulove.viewmodel.ReadingViewModel


class LoadingDialogFragment : DialogFragment() {

    private val newReadingVM : NewReadingBookViewModel by viewModels ({requireParentFragment()})
    private val readingVM : ReadingViewModel by activityViewModels()
    private val detailReadingVM : DetailReadingViewModel by viewModels({requireParentFragment()})
    private val endedVM : EndedViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        // Make Dialog Incancellable: This dialog is used in order to wait a daabase operation
        isCancelable = false
        setObservers()
        return inflater.inflate(R.layout.fragment_loading_dialog_fragment, container, false)
    }

    private fun setObservers() {
        val isUpdatingObserver = Observer<Boolean> { newUpdating ->
            if (newUpdating == false) dialog?.cancel()
        }
        newReadingVM.isAccessingDatabase.observe(viewLifecycleOwner, isUpdatingObserver)
        readingVM.isAccessingDatabase.observe(viewLifecycleOwner, isUpdatingObserver)
        detailReadingVM.isAccessingDatabase.observe(viewLifecycleOwner, isUpdatingObserver)
        endedVM.isAccessingDatabase.observe(viewLifecycleOwner, isUpdatingObserver)
    }

}