package it.simone.bookyoulove.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.EndDate
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.databinding.FragmentModifyEndedBinding
import it.simone.bookyoulove.view.dialog.CoverLinkPickerFragment
import it.simone.bookyoulove.view.dialog.DatePickerFragment
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.view.dialog.PagesPickerFragment
import it.simone.bookyoulove.viewmodel.ChartsViewModel
import it.simone.bookyoulove.viewmodel.EndedViewModel
import it.simone.bookyoulove.viewmodel.ModifyEndedViewModel
import java.time.Month
import java.time.format.TextStyle
import java.util.*


class ModifyEndedFragment : Fragment(), View.OnClickListener, RatingBar.OnRatingBarChangeListener {

    private lateinit var binding : FragmentModifyEndedBinding

    private val endedVM : EndedViewModel by activityViewModels()
    private val chartsVM : ChartsViewModel by activityViewModels()
    private val modifyEndedVM : ModifyEndedViewModel by viewModels()

    private var loadingDialog = LoadingDialogFragment()

    private val args: ModifyEndedFragmentArgs by navArgs()

    private lateinit var currentStartDate : StartDate
    private lateinit var currentEndDate : EndDate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childFragmentManager.setFragmentResultListener("startDateKey", this, { _, bundle ->
            val newStartDate = StartDate(bundle.getInt("day"), bundle.getInt("month"), bundle.getInt("year"))
            currentStartDate = newStartDate

            binding.modifyEndedStartDateText.text = findStartDateString(newStartDate)

            modifyEndedVM.modifyStartDate(newStartDate)
        })

        childFragmentManager.setFragmentResultListener("endDateKey", this, { _, bundle ->
            val newEndDate = EndDate(bundle.getInt("day"), bundle.getInt("month"), bundle.getInt("year"))
            currentEndDate = newEndDate

            binding.modifyEndedEndDateText.text = findEndDateString(newEndDate)

            modifyEndedVM.modifyEndDate(newEndDate)
        })

        childFragmentManager.setFragmentResultListener("pagesKey", this, {_ , bundle ->
            val newPages = bundle.getInt("settedPages")
            modifyEndedVM.modifyPages(newPages)
            binding.modifyEndedPagesText.text = newPages.toString()
        })

        childFragmentManager.setFragmentResultListener("coverLinkKey", this, {_, bundle ->
            val newLink = bundle.getString("settedCoverLink")

            if (newLink != "") Picasso.get().load(newLink).placeholder(R.drawable.book_cover_place_holder).error(
                R.drawable.cover_not_found).into(binding.modifyEndedCoverImageView)
            else Picasso.get().load(R.drawable.book_cover_place_holder).into(binding.modifyEndedCoverImageView)

            modifyEndedVM.modifyCover(newLink!!)
        })

        //Controllo se non null perché nel caso in cui si faccia direttamente l'aggiunta di un libro Ended il parametro è null
        if (args.modifyEndedKeyTitle != null) modifyEndedVM.setBookToModify(args.modifyEndedKeyTitle!!, args.modifyEndedKeyAuthor!!, args.modifyEndedTime)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentModifyEndedBinding.inflate(inflater, container, false)

        binding.modifyEndedCoverImageView.setOnClickListener(this)
        binding.modifyEndedStartDateCard.setOnClickListener(this)
        binding.modifyEndedEndDateCard.setOnClickListener(this)
        binding.modifyEndedPaperCheck.setOnClickListener(this)
        binding.modifyEndedEbookCheck.setOnClickListener(this)
        binding.modifyEndedAudiobookCheck.setOnClickListener(this)
        binding.modifyEndedPagesCard.setOnClickListener(this)
        binding.modifyEndedSaveButton.setOnClickListener(this)

        binding.endedModifyTotalRate.onRatingBarChangeListener = this
        binding.endedModifyStyleRate.onRatingBarChangeListener = this
        binding.endedModifyEmotionsRate.onRatingBarChangeListener = this
        binding.endedModifyPlotRate.onRatingBarChangeListener = this
        binding.endedModifyCharactersRate.onRatingBarChangeListener = this

        setObservers()

        return binding.root
    }


    private fun setObservers() {

        val currentBookObserver = Observer<Book> { currentBook ->
            binding.modifyEndedTitleText.text = currentBook.title
            binding.modifyEndedAuthorText.text = currentBook.author

            if (currentBook.coverName != "") Picasso.get().load(currentBook.coverName).placeholder(R.drawable.book_cover_place_holder).error(
                R.drawable.cover_not_found).into(binding.modifyEndedCoverImageView)
            else Picasso.get().load(R.drawable.book_cover_place_holder).into(binding.modifyEndedCoverImageView)

            binding.modifyEndedPagesText.text = currentBook.pages.toString()

            binding.endedModifyTotalRate.rating = currentBook.rate?.totalRate!!
            binding.endedModifyStyleRate.rating = currentBook.rate?.totalRate!!
            binding.endedModifyEmotionsRate.rating = currentBook.rate?.totalRate!!
            binding.endedModifyPlotRate.rating = currentBook.rate?.totalRate!!
            binding.endedModifyCharactersRate.rating = currentBook.rate?.totalRate!!

            binding.modifyEndedPaperCheck.isChecked = currentBook.support?.paperSupport ?: false
            binding.modifyEndedEbookCheck.isChecked = currentBook.support?.ebookSupport ?: false
            binding.modifyEndedAudiobookCheck.isChecked = currentBook.support?.audiobookSupport ?: false

            currentStartDate = currentBook.startDate!!
            binding.modifyEndedStartDateText.text = findStartDateString(currentBook.startDate)

            currentEndDate = currentBook.endDate!!
            binding.modifyEndedEndDateText.text = findEndDateString(currentBook.endDate)
        }
        modifyEndedVM.currentBook.observe(viewLifecycleOwner, currentBookObserver)

        val canExitObserver = Observer<Boolean> { canExit ->
            if (canExit) {
                //Notifico il cambiamento del SINGOLO item della lista
                if (modifyEndedVM.finalBook != null) endedVM.notifyArrayItemChanged(modifyEndedVM.finalBook!!)
                chartsVM.changeLoadedStatus()
                //requireActivity().onBackPressed()
                val navController = findNavController()
                val action = ModifyEndedFragmentDirections.actionGlobalReadListFragment()
                navController.navigate(action)
            }
        }
        modifyEndedVM.canExit.observe(viewLifecycleOwner, canExitObserver)

        val isAccessingObserver = Observer<Boolean> {isAccessing ->
            if (isAccessing) {
                loadingDialog.showNow(childFragmentManager, "Loading Dialog")
            }
            else {
                if (loadingDialog.isAdded) {
                    loadingDialog.dismiss()
                    loadingDialog = LoadingDialogFragment()
                }
            }
        }
        modifyEndedVM.isAccessingDatabase.observe(viewLifecycleOwner, isAccessingObserver)
    }


    override fun onClick(view: View?) {

        when (view) {

            binding.modifyEndedStartDateCard -> {
                val datePickerDialog = DatePickerFragment()
                datePickerDialog.arguments = bundleOf("caller" to START_DATE_SETTER,
                "maxDay" to currentEndDate.endDay,
                "maxMonth" to currentEndDate.endMonth,
                "maxYear" to currentEndDate.endYear)
                datePickerDialog.show(childFragmentManager, "Start Date Picker")
            }

            binding.modifyEndedEndDateCard -> {
                val datePickerDialog = DatePickerFragment()
                datePickerDialog.arguments = bundleOf("caller" to END_DATE_SETTER,
                    "minDay" to currentStartDate.startDay,
                    "minMonth" to currentStartDate.startMonth,
                    "minYear" to currentStartDate.startYear)
                datePickerDialog.show(childFragmentManager, "End Date Picker")
            }

            binding.modifyEndedPagesCard -> {
                PagesPickerFragment().show(childFragmentManager, "Pages Picker")
            }

            binding.modifyEndedCoverImageView -> {
                CoverLinkPickerFragment().show(childFragmentManager, "Cover Link Picker")
            }

            binding.modifyEndedPaperCheck , binding.modifyEndedEbookCheck, binding.modifyEndedAudiobookCheck -> {
                val paperCheck = binding.modifyEndedPaperCheck.isChecked
                val ebookCheck = binding.modifyEndedEbookCheck.isChecked
                val audiobookCheck = binding.modifyEndedAudiobookCheck.isChecked

                val supportMap = mapOf(PAPER_SUPPORT to paperCheck , EBOOK_SUPPORT to ebookCheck, AUDIOBOOK_SUPPORT to audiobookCheck)

                modifyEndedVM.modifySupport(supportMap)
            }

            binding.modifyEndedSaveButton -> {
                modifyEndedVM.saveModifiedBook()
            }
        }
    }


    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
        var modifiedRate = 0
        when (ratingBar) {
            binding.endedModifyTotalRate -> modifiedRate = 0
            binding.endedModifyStyleRate -> modifiedRate = 1
            binding.endedModifyEmotionsRate -> modifiedRate = 2
            binding.endedModifyPlotRate -> modifiedRate = 3
            binding.endedModifyCharactersRate -> modifiedRate = 4
        }
        modifyEndedVM.modifyRate(rating, modifiedRate)
    }


    private fun findStartDateString(startDate : StartDate?) : String {
        return "${startDate!!.startDay} ${
            Month.of(startDate.startMonth).getDisplayName(
                TextStyle.FULL, Locale.getDefault()).capitalize(Locale.getDefault())
        } ${startDate.startYear}"
    }

    private fun findEndDateString(endDate: EndDate?) : String {
        return "${endDate!!.endDay} ${
            Month.of(endDate.endMonth).getDisplayName(
                TextStyle.FULL, Locale.getDefault()).capitalize(Locale.getDefault())
        } ${endDate.endYear}"
    }

}