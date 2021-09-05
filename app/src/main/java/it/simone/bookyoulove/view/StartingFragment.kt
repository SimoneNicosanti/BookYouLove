package it.simone.bookyoulove.view

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.databinding.FragmentStartingBinding
import it.simone.bookyoulove.utilsClass.DateFormatClass
import it.simone.bookyoulove.view.dialog.DatePickerFragment
import it.simone.bookyoulove.viewmodel.BookListViewModel
import it.simone.bookyoulove.viewmodel.StartingViewModel


class StartingFragment : Fragment() , View.OnClickListener{

    private lateinit var binding : FragmentStartingBinding

    private val args : StartingFragmentArgs by navArgs()

    private val readingVM : BookListViewModel by activityViewModels()
    private val startingVM : StartingViewModel by viewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentStartingBinding.inflate(inflater, container, false)
        setViewEnable(true, requireActivity())

        startingVM.loadStartingBook(args.startingBookId)

        setObservers()

        binding.startingStartDateCard.setOnClickListener(this)
        binding.startingFragmentSaveButton.setOnClickListener(this)
        binding.startingFragmentPaperCheckbox.setOnClickListener(this)
        binding.startingFragmentEbookCheckbox.setOnClickListener(this)
        binding.startingFragmentAudiobookCheckbox.setOnClickListener(this)

        childFragmentManager.setFragmentResultListener("startDateKey", this) {_ , bundle ->
            val newStartDate = bundle.getLong("dateMillis")
            binding.startingStartDateTextView.text = DateFormatClass(requireContext()).computeDateString(newStartDate)
            startingVM.changeStartDate(newStartDate)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setObservers() {
        val isAccessingObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                setViewEnable(false, requireActivity())
                binding.startingLoading.root.visibility = View.VISIBLE
            }

            else {
                setViewEnable(true, requireActivity())
                binding.startingLoading.root.visibility = View.GONE
            }
        }
        startingVM.isAccessing.observe(viewLifecycleOwner, isAccessingObserver)

        val canExitWithBookObserver = Observer<Book> {
            readingVM.notifyNewArrayItem(it)
            findNavController().previousBackStackEntry?.savedStateHandle?.set("startedTbrBookKey", true)
            findNavController().popBackStack()
        }
        startingVM.canExitWithBook.observe(viewLifecycleOwner, canExitWithBookObserver)

        val currentBookObserver = Observer<Book> {
            binding.startingStartDateTextView.text = DateFormatClass(requireContext()).computeDateString(it.startDate!!)

            binding.startingFragmentPaperCheckbox.isChecked = it.support!!.paperSupport
            binding.startingFragmentEbookCheckbox.isChecked = it.support!!.ebookSupport
            binding.startingFragmentAudiobookCheckbox.isChecked = it.support!!.audiobookSupport
        }
        startingVM.currentStartingBook.observe(viewLifecycleOwner, currentBookObserver)
    }

    override fun onClick(v: View?) {

        when (v) {
            binding.startingStartDateCard -> {
                val startDatePicker = DatePickerFragment()
                val args = bundleOf("caller" to START_DATE_SETTER)
                startDatePicker.arguments = args
                startDatePicker.show(childFragmentManager, "Start Date Picker")
            }

            binding.startingFragmentPaperCheckbox, binding.startingFragmentEbookCheckbox, binding.startingFragmentAudiobookCheckbox -> {
                startingVM.changeSupport(mapOf(
                    PAPER_SUPPORT to binding.startingFragmentPaperCheckbox.isChecked,
                    EBOOK_SUPPORT to binding.startingFragmentEbookCheckbox.isChecked,
                    AUDIOBOOK_SUPPORT to binding.startingFragmentAudiobookCheckbox.isChecked
                ))
            }

            binding.startingFragmentSaveButton -> {
                startingVM.setBookAsStarted()
            }
        }
    }

}