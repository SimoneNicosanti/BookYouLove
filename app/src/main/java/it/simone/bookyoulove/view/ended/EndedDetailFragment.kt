package it.simone.bookyoulove.view.ended

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.databinding.FragmentEndedDetailBinding
import it.simone.bookyoulove.view.QUOTE_LIST_ENDED_CALLER
import it.simone.bookyoulove.view.dialog.ConfirmDeleteDialogFragment
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.ChartsViewModel
import it.simone.bookyoulove.viewmodel.DetailEndedViewModel
import it.simone.bookyoulove.viewmodel.EndedViewModel
import java.time.Month
import java.time.format.TextStyle
import java.util.*

//

class EndedDetailFragment : Fragment(), View.OnClickListener {

    private lateinit var binding : FragmentEndedDetailBinding
    private val endedDetailVM : DetailEndedViewModel by viewModels()
    private val endedVM : EndedViewModel by activityViewModels()
    private val chartsVM : ChartsViewModel by activityViewModels()

    private var loadingDialog = LoadingDialogFragment()

    private val args : EndedDetailFragmentArgs by navArgs()

    private lateinit var endedFinalThought : String

    private lateinit var endedDetailBook : Book


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childFragmentManager.setFragmentResultListener("deleteKey", this) { _, bundle ->
            if (bundle.getBoolean("deleteConfirm")) {
                endedDetailVM.deleteCurrentBook()
                //Notifico la cancellazione per il grafico dei charts
                chartsVM.changeLoadedStatus()
            }
        }

        endedDetailVM.loadEndedDetailBook(args.endedDetailKeyTitle, args.endedDetailKeyAuthor, args.endedDetailTime)

        setHasOptionsMenu(true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentEndedDetailBinding.inflate(inflater, container, false)

        binding.endedDetailFinalThoughtButton.setOnClickListener(this)
        binding.endedDetailYourQuotesButton.setOnClickListener(this)

        setObservers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("changedFinalThoughtKey")?.observe(viewLifecycleOwner) { changedFinalThought ->
            endedFinalThought = changedFinalThought
            endedDetailVM.changeThought(changedFinalThought)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("changedFinalThoughtKey")
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Book>("endedModifiedBook")?.observe(viewLifecycleOwner) { changedBook ->
            endedDetailVM.onEndedBookChanged(changedBook)
            endedVM.notifyArrayItemChanged(changedBook)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Book>("endedModifiedBook")
        }
    }

    private fun setObservers() {

        val isAccessingDatabaseObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.endedDetailLoading.root.visibility = View.VISIBLE
            }

            else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.endedDetailLoading.root.visibility = View.GONE
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

            endedDetailBook = currentBook
        }
        endedDetailVM.currentBook.observe(viewLifecycleOwner, currentBookObserver)

        val deleteCompletedObserver = Observer<Boolean> { completed ->
            if (completed) {
                endedVM.notifyArrayItemDelete()
                findNavController().popBackStack()
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
                val arguments = bundleOf("itemToDelete" to resources.getString(R.string.book_string))
                val confirmDeleteDialog = ConfirmDeleteDialogFragment()
                confirmDeleteDialog.arguments = arguments
                confirmDeleteDialog.show(childFragmentManager, "Delete Confirm")
                true
            }

            R.id.endedDetailMenuEditItem -> {
                val navController = findNavController()
                val action = EndedDetailFragmentDirections.actionEndedDetailFragmentToModifyEndedFragment(endedDetailBook)
                navController.navigate(action)
                true
            }

            R.id.endedDetailMenuTakeNoteItem -> {
                findNavController().navigate(EndedDetailFragmentDirections.actionGlobalModifyQuoteFragment(endedDetailBook.title, endedDetailBook.author, endedDetailBook.readTime, null))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onClick(v: View?) {
         when (v) {
             binding.endedDetailYourQuotesButton -> {
                 findNavController().navigate(EndedDetailFragmentDirections.actionGlobalQuoteListFragment(endedDetailBook.keyTitle, endedDetailBook.keyAuthor, endedDetailBook.readTime))
             }

             binding.endedDetailFinalThoughtButton -> {
                 val navController = findNavController()
                 val action = EndedDetailFragmentDirections.actionEndedDetailFragmentToEndedThoughtFragment(endedFinalThought, args.endedDetailKeyTitle, args.endedDetailKeyAuthor, args.endedDetailTime)
                 navController.navigate(action)
             }
         }
    }
}