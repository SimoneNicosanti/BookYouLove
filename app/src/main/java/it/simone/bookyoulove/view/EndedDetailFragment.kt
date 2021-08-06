package it.simone.bookyoulove.view

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.PagerSnapHelper
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.databinding.FragmentEndedDetailBinding
import it.simone.bookyoulove.view.dialog.ConfirmDeleteDialogFragment
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.DetailEndedViewModel
import it.simone.bookyoulove.viewmodel.EndedViewModel
import java.time.Month
import java.time.format.TextStyle
import java.util.*

//

class EndedDetailFragment : Fragment() {

    private lateinit var binding : FragmentEndedDetailBinding
    private val endedDetailVM : DetailEndedViewModel by viewModels()
    private val endedVM : EndedViewModel by activityViewModels()

    private var loadingDialog = LoadingDialogFragment()

    private val args : EndedDetailFragmentArgs by navArgs()

    private lateinit var endedFinalThought : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childFragmentManager.setFragmentResultListener("deleteKey", this) { _, bundle ->
            if (bundle.getBoolean("deleteConfirm")) endedDetailVM.deleteCurrentBook()
        }

        endedDetailVM.loadEndedDetailBook(args.endedDetailTitle, args.endedDetailAuthor, args.endedDetailTime)

        setHasOptionsMenu(true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentEndedDetailBinding.inflate(inflater, container, false)

        binding.endedDetailFinalThoughtButton.setOnClickListener {
            val navController = findNavController()
            val action = EndedDetailFragmentDirections.actionEndedDetailFragmentToEndedThoughtFragment(endedFinalThought, args.endedDetailTitle, args.endedDetailAuthor, args.endedDetailTime)
            navController.navigate(action)
        }

        setObservers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("changedFinalThoughtKey")?.observe(viewLifecycleOwner) { changedFinalThought ->
            endedFinalThought = changedFinalThought
            endedDetailVM.changeThought(changedFinalThought)
        }
    }

    private fun setObservers() {

        val isAccessingDatabaseObserver = Observer<Boolean> {
            if (it) {
                loadingDialog.showNow(childFragmentManager, "Loading Dialog")
            }
            else {
                if (loadingDialog.isAdded) {
                    loadingDialog.dismiss()
                    loadingDialog = LoadingDialogFragment()
                }
            }
        }
        endedDetailVM.isAccessingDatabase.observe(viewLifecycleOwner, isAccessingDatabaseObserver)

        val currentBookObserver = Observer<Book>  { currentBook ->
            Log.i("Nicosanti", "Current Book")
            binding.endedDetailTitle.text = currentBook.title
            binding.endedDetailAuthor.text = currentBook.author

            if (currentBook.coverName != "") Picasso.get().load(currentBook.coverName).placeholder(R.drawable.book_cover_place_holder).error(R.drawable.cover_not_found).into(binding.endedDetailCoverImageView)
            else Picasso.get().load(R.drawable.book_cover_place_holder).into(binding.endedDetailCoverImageView)

            binding.endedDetailPagesTextView.text = currentBook.pages.toString()

            binding.endedDetailTotalRate.rating = currentBook.rate?.totalRate!!
            binding.endedDetailStyleRate.rating = currentBook.rate?.styleRate!!
            binding.endedDetailEmotionsRate.rating = currentBook.rate?.emotionRate!!
            binding.endedDetailPlotRate.rating = currentBook.rate?.plotRate!!
            binding.endedDetailCharactersRate.rating = currentBook.rate?.characterRate!!

            binding.endedDetailPaperCheckBox.isChecked = currentBook.support?.paperSupport ?: false
            binding.endedDetailEbookCheckBox.isChecked = currentBook.support?.ebookSupport ?: false
            binding.endedDetailAudiobookCheckBox.isChecked = currentBook.support?.audiobookSupport ?: false

            val startDateText = "${currentBook.startDate?.startDay} ${Month.of(currentBook.startDate!!.startMonth).getDisplayName(TextStyle.FULL, Locale.getDefault()).capitalize(
                Locale.ROOT
            )
            } ${currentBook.startDate?.startYear}"
            binding.endedDetailStartDateTextView.text = startDateText

            val endDateText = "${currentBook.endDate?.endDay} ${Month.of(currentBook.endDate!!.endMonth).getDisplayName(TextStyle.FULL, Locale.getDefault()).capitalize(
                Locale.ROOT
            )
            } ${currentBook.endDate?.endYear}"
            binding.endedDetailEndDateTextView.text = endDateText

            endedFinalThought = currentBook.finalThought

            //setRadarChart(currentBook.rate!!)
        }
        endedDetailVM.currentBook.observe(viewLifecycleOwner, currentBookObserver)

        val deleteCompletedObserver = Observer<Boolean> { completed ->
            if (completed) {
                endedVM.notifyArrayItemDelete()
                requireActivity().onBackPressed()
            }
        }
        endedDetailVM.deleteCompleted.observe(viewLifecycleOwner, deleteCompletedObserver)
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.ended_detail_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.endedDetailMenuDeleteItem -> {
                ConfirmDeleteDialogFragment().show(childFragmentManager, "Delete Confirm")
                true
            }

            R.id.endedDetailMenuEditItem -> {
                val navController = findNavController()
                val action = EndedDetailFragmentDirections.actionEndedDetailFragmentToModifyEndedFragment(args.endedDetailTitle, args.endedDetailAuthor, args.endedDetailTime)
                navController.navigate(action)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }
}