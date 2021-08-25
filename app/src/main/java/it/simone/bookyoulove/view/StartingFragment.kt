package it.simone.bookyoulove.view

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.databinding.FragmentStartingBinding
import it.simone.bookyoulove.view.dialog.DatePickerFragment
import it.simone.bookyoulove.viewmodel.ReadingViewModel
import it.simone.bookyoulove.viewmodel.StartingViewModel
import java.time.Month
import java.time.format.TextStyle
import java.util.*


class StartingFragment : Fragment() , View.OnClickListener{

    private lateinit var binding : FragmentStartingBinding

    private val args : StartingFragmentArgs by navArgs()

    private val readingVM : ReadingViewModel by activityViewModels()
    private val startingVM : StartingViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentStartingBinding.inflate(inflater, container, false)

        startingVM.loadStartingBook(args.startingBookId)

        setObservers()

        binding.startingStartDateCard.setOnClickListener(this)
        binding.startingFragmentSaveButton.setOnClickListener(this)
        binding.startingFragmentPaperCheckbox.setOnClickListener(this)
        binding.startingFragmentEbookCheckbox.setOnClickListener(this)
        binding.startingFragmentAudiobookCheckbox.setOnClickListener(this)

        childFragmentManager.setFragmentResultListener("startDateKey", this) {_ , bundle ->
            val newStartDate = StartDate(bundle.getInt("day"), bundle.getInt("month"), bundle.getInt("year"))
            binding.startingStartDateTextView.text = computeDateString(newStartDate)
            startingVM.changeStartDate(newStartDate)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun setObservers() {
        val isAccessingObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.startingLoading.root.visibility = View.VISIBLE
            }

            else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.startingLoading.root.visibility = View.GONE
            }
        }
        startingVM.isAccessing.observe(viewLifecycleOwner, isAccessingObserver)

        val canExitWithBookObserver = Observer<Book> {
            readingVM.notifyNewReadingBook(it)
            findNavController().previousBackStackEntry?.savedStateHandle?.set("startedTbrBookKey", true)
            findNavController().popBackStack()
        }
        startingVM.canExitWithBook.observe(viewLifecycleOwner, canExitWithBookObserver)

        val currentBookObserver = Observer<Book> {
            binding.startingStartDateTextView.text = computeDateString(it.startDate!!)

            binding.startingFragmentPaperCheckbox.isChecked = it.support!!.paperSupport
            binding.startingFragmentEbookCheckbox.isChecked = it.support!!.ebookSupport
            binding.startingFragmentAudiobookCheckbox.isChecked = it.support!!.audiobookSupport
        }
        startingVM.currentStartingBook.observe(viewLifecycleOwner, currentBookObserver)
    }

    private fun computeDateString(startDate: StartDate): String {
        return "${startDate.startDay} ${
            Month.of(startDate.startMonth).getDisplayName(
                TextStyle.FULL, Locale.getDefault()).capitalize(Locale.getDefault())
        } ${startDate.startYear}"
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