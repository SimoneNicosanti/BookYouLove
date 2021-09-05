package it.simone.bookyoulove.view.ended

import android.content.pm.ActivityInfo
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
import it.simone.bookyoulove.utilsClass.DateFormatClass
import it.simone.bookyoulove.view.dialog.ConfirmDeleteDialogFragment
import it.simone.bookyoulove.view.setViewEnable
import it.simone.bookyoulove.viewmodel.DetailBookViewModel
import it.simone.bookyoulove.viewmodel.charts.ChartsViewModel


class EndedDetailFragment : Fragment(), View.OnClickListener {

    private lateinit var binding : FragmentEndedDetailBinding
    private val endedDetailVM : DetailBookViewModel by viewModels()
    //private val endedVM : EndedViewModel by activityViewModels()
    private val chartsVM : ChartsViewModel by activityViewModels()


    private val args : EndedDetailFragmentArgs by navArgs()

    private lateinit var endedFinalThought : String

    private lateinit var endedDetailBook : Book


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setViewEnable(true, requireActivity())

        childFragmentManager.setFragmentResultListener("deleteKey", this) { _, bundle ->
            if (bundle.getBoolean("deleteConfirm")) {
                endedDetailVM.deleteCurrentBook()
            }
        }

        endedDetailVM.loadDetailBook(args.endedBookId)

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
            endedDetailVM.onBookModified(changedBook)
            findNavController().previousBackStackEntry?.savedStateHandle?.set("endedModifiedBookKey", changedBook)          //Comunico al precedente la modifica del libro
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Book>("endedModifiedBook")
        }

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setObservers() {

        val isAccessingDatabaseObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                setViewEnable(false, requireActivity())
                binding.endedDetailLoading.root.visibility = View.VISIBLE
            }

            else {
                setViewEnable(true, requireActivity())
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

            val startDateText = DateFormatClass(requireContext()).computeDateString(currentBook.startDate)
            binding.endedDetailStartDateTextView.text = startDateText

            val endDateText = DateFormatClass(requireContext()).computeDateString(currentBook.endDate)
            binding.endedDetailEndDateTextView.text = endDateText

            endedFinalThought = currentBook.finalThought

            endedDetailBook = currentBook
        }
        endedDetailVM.currentBook.observe(viewLifecycleOwner, currentBookObserver)

        val deleteCompletedObserver = Observer<Boolean> { completed ->
            if (completed) {
                //endedVM.notifyArrayItemDelete()
                chartsVM.changeLoadedStatus()
                findNavController().previousBackStackEntry?.savedStateHandle?.set("deleteKey", true)        //Comunico la cancellazione alla lista degli Ended
                findNavController().popBackStack()
            }
        }
        endedDetailVM.deleteCompleted.observe(viewLifecycleOwner, deleteCompletedObserver)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.ended_detail_fragment_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        //Nel caso in cui si entri in questo fragment partendo da ChartsFragment, non permetto all'utente di modificare il libro, ma solo di visualizzarlo
        if (args.endedDetailEntryPoint != 0) {
            menu.removeItem(R.id.endedDetailMenuEditItem)
            menu.removeItem(R.id.endedDetailMenuDeleteItem)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.endedDetailMenuDeleteItem -> {
                val arguments = bundleOf("itemToDelete" to resources.getString(R.string.delete_book_dialog_title))
                val confirmDeleteDialog = ConfirmDeleteDialogFragment()
                confirmDeleteDialog.arguments = arguments
                confirmDeleteDialog.show(childFragmentManager, "Delete Confirm")
                true
            }

            R.id.endedDetailMenuEditItem -> {
                val navController = findNavController()
                val action = EndedDetailFragmentDirections.actionEndedDetailFragmentToModifyEndedFragment(endedDetailBook.copy(
                        support = endedDetailBook.support?.copy(),
                        rate = endedDetailBook.rate?.copy()
                ))
                navController.navigate(action)
                true
            }

            R.id.endedDetailMenuTakeNoteItem -> {
                findNavController().navigate(EndedDetailFragmentDirections.actionGlobalModifyQuoteFragment(
                    null,
                    endedDetailBook.bookId,
                    endedDetailBook.title,
                    endedDetailBook.author))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onClick(v: View?) {
         when (v) {
             binding.endedDetailYourQuotesButton -> {
                 findNavController().navigate(EndedDetailFragmentDirections.actionGlobalQuoteListFragment(args.endedBookId))
             }

             binding.endedDetailFinalThoughtButton -> {
                 val navController = findNavController()
                 val action = EndedDetailFragmentDirections.actionEndedDetailFragmentToEndedThoughtFragment(endedFinalThought, args.endedBookId)
                 navController.navigate(action)
             }
         }
    }
}