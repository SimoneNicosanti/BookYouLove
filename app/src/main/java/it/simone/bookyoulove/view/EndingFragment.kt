package it.simone.bookyoulove.view

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.RatingBar
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.EndDate
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.databinding.FragmentEndingBinding
import it.simone.bookyoulove.view.dialog.DatePickerFragment
import it.simone.bookyoulove.viewmodel.charts.ChartsViewModel
import it.simone.bookyoulove.viewmodel.ended.EndedViewModel
import it.simone.bookyoulove.viewmodel.EndingViewModel
import it.simone.bookyoulove.viewmodel.reading.ReadingViewModel
import java.time.Month
import java.time.format.TextStyle
import java.util.*


class EndingFragment : Fragment(), View.OnClickListener, RatingBar.OnRatingBarChangeListener {

    private lateinit var binding : FragmentEndingBinding
    private val endingVM : EndingViewModel by viewModels()
    private val readingVM : ReadingViewModel by activityViewModels()
    private val endedVM : EndedViewModel by activityViewModels()
    private val chartsVM: ChartsViewModel by activityViewModels()

    private val args : EndingFragmentArgs by navArgs()

    private lateinit var terminateStartDate : StartDate


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childFragmentManager.setFragmentResultListener("endDateKey", this, { _, bundle ->
            val settedEndDate = EndDate(bundle.getInt("day"), bundle.getInt("month"), bundle.getInt("year"))
            binding.endingEndDateText.text = computeEndDateString(settedEndDate)
            endingVM.setEndDate(settedEndDate)
        })

        endingVM.loadEndingBook(args.endingBookid)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEndingBinding.inflate(inflater, container, false)

        binding.endingFlipRateCardButton.setOnClickListener(this)
        binding.endingEndDateCard.setOnClickListener(this)
        binding.endingSaveButton.setOnClickListener(this)
        binding.endingTotalRate.onRatingBarChangeListener = this
        binding.endingStyleRate.onRatingBarChangeListener = this
        binding.endingEmotionsRate.onRatingBarChangeListener = this
        binding.endingPlotRate.onRatingBarChangeListener = this
        binding.endingCharactersRate.onRatingBarChangeListener = this

        binding.endingFinalThoughtInput.doOnTextChanged { text, _, _, _ ->
            endingVM.setFinalThought(text)
        }

        setObservers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }


    private fun setObservers() {

        val terminateBookObserver = Observer<Book?> { terminateBook ->
            binding.endingEndDateText.text = computeEndDateString(terminateBook.endDate!!)
            terminateStartDate = terminateBook.startDate!!
        }
        endingVM.terminateBook.observe(viewLifecycleOwner, terminateBookObserver)

        val canExitObserver = Observer<Boolean> { canExit ->
            if (canExit) {
                readingVM.notifyBookTerminated()
                endedVM.setEndedListChanged(true)
                chartsVM.changeLoadedStatus()        //Comunico il cambiamento nella lista di libri letti di modo che venga ricaricata da charts
                findNavController().popBackStack()
            }
        }
        endingVM.canExit.observe(viewLifecycleOwner, canExitObserver)

        val isAccessingDatabaseObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.endingLoading.root.visibility = View.VISIBLE
            }

            else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.endingLoading.root.visibility = View.GONE
            }
        }
        endingVM.isAccessingDatabase.observe(viewLifecycleOwner, isAccessingDatabaseObserver)
    }


    private fun computeEndDateString(endDate: EndDate): String {
        return "${endDate.endDay} ${
            Month.of(endDate.endMonth).getDisplayName(
                    TextStyle.FULL, Locale.getDefault()).capitalize(Locale.getDefault())
        } ${endDate.endYear}"
    }


    override fun onClick(view: View?) {

        when (view) {

            binding.endingFlipRateCardButton -> {
                if (!endingVM.isFlipped) {
                    binding.otherRatesLinearLayout.visibility = View.VISIBLE
                    (view as ImageButton).setImageResource(R.drawable.ic_round_arrow_drop_down)
                }
                else {
                    binding.otherRatesLinearLayout.visibility = View.GONE
                    (view as ImageButton).setImageResource(R.drawable.ic_round_arrow_drop_up)
                }
                endingVM.isFlipped = !endingVM.isFlipped
            }

            binding.endingEndDateCard -> {
                val datePickerFragment = DatePickerFragment()
                val minDate = terminateStartDate
                datePickerFragment.arguments = bundleOf("caller" to END_DATE_SETTER,
                        "minDay" to minDate.startDay,
                        "minMonth" to minDate.startMonth,
                        "minYear" to minDate.startYear)
                datePickerFragment.show(childFragmentManager, "End Date Picker")
            }

            binding.endingSaveButton -> {
                endingVM.saveTerminatedBook()
            }
        }
    }


    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {

        when (ratingBar) {
            binding.endingTotalRate -> endingVM.modifyTotalRate(rating)
            binding.endingStyleRate -> endingVM.modifyStyleRate(rating)
            binding.endingEmotionsRate -> endingVM.modifyEmotionsRate(rating)
            binding.endingPlotRate -> endingVM.modifyPlotRate(rating)
            binding.endingCharactersRate -> endingVM.modifyCharactersRate(rating)
        }
    }
}