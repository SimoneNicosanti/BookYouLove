package it.simone.bookyoulove.view

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.EndDate
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.databinding.FragmentReadingBinding
import it.simone.bookyoulove.view.dialog.EndBookDialogFragment
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.EndedViewModel
import it.simone.bookyoulove.viewmodel.ReadingViewModel

/*
    La lista viene ricaricata da DB ogni volta che la schermata viene creata: il motivo di ciò è che
    il VM è legato al fragment. Se esco dal fragment, il viewModel muore con lui; quando ci rientro, anche
    se la lista non è stata modificata da nessuno, il viewModel viene reistanziato e la lista viene riletta.
    Un modo semplice per risolvere è associare il VM non al fragment, ma all'activity: in questo modo il ViewModel
    non viene deistanziato e quando il fragment si riattiva, se la lista non è stata modificata, allora i libri che il
    viewmodel dice di mostrare sono sempre quelli, altimenti saranno cambiati
    --> ASSOCIA readingViewModel ad Activity
    A quel punto, dopo averla associata all'activity, si può anche rimuovere updatedDatabaseVM e portare le variabili
    sull'aggiornamento nei singoli viewModel legati all'activity
 */

//TODO("Inserire meccanismo di cancelazione del libro")

class ReadingFragment : Fragment() , View.OnClickListener{

    private lateinit var binding: FragmentReadingBinding

    private lateinit var navController: NavController

    //Variabile che mantiene le informazioni del libro da mostrare
    private var showBookInfo : Book? = null

    private var loadingDialog = LoadingDialogFragment()

    private val readingVM: ReadingViewModel by activityViewModels()
    private val endedVM: EndedViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        childFragmentManager.setFragmentResultListener("endBookInfo", this) { _, bundle ->
            val settedRate = bundle.getFloat("settedRate")
            val endDay = bundle.getInt("endDay")
            val endMonth = bundle.getInt("endMonth")
            val endYear = bundle.getInt("endYear")

            val endDate = EndDate(endDay, endMonth, endYear)

            val startDate : StartDate = (showBookInfo?.startDate!!)

            var newSnackbar : Snackbar? = null
            if (endDate.endYear < startDate.startYear) newSnackbar = Snackbar.make(requireView(), R.string.invalid_date_string, Snackbar.LENGTH_SHORT)
            if (endDate.endYear == startDate.startYear && endDate.endMonth < startDate.startMonth) newSnackbar = Snackbar.make(requireView(), R.string.invalid_date_string, Snackbar.LENGTH_SHORT)
            if (endDate.endYear == startDate.startYear && endDate.endMonth == startDate.startMonth && endDate.endDay < startDate.startDay) newSnackbar = Snackbar.make(requireView(), R.string.invalid_date_string, Snackbar.LENGTH_SHORT)

            if (newSnackbar != null) {
                newSnackbar.setAnchorView(R.id.bottomNavigationView)
                newSnackbar.show()
            }

            else {
                readingVM.terminateBook(endDate, settedRate)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentReadingBinding.inflate(inflater, container, false)

        binding.nextBookButton.setOnClickListener(this)
        binding.prevBookButton.setOnClickListener(this)
        //binding.readingCoverImageView.setOnClickListener(this)
        //binding.readingTerminateBtn.setOnClickListener(this)

        registerForContextMenu(binding.readingCoverImageView)

        setObservers()

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = view.findNavController()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reading_fragment_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.readingNewItem -> {
                val action = ReadingFragmentDirections.actionReadingFragmentToNewReadingBookFragment()
                navController.navigate(action)
            }
        }
        return true
    }


    private fun setObservers() {

        val readingListStateObserver = Observer<Boolean> { changed ->
            //Se la lista nel DB è aggiornata
            if (changed) {
                //Invoco la lettura
                readingVM.restartShowedBook()
                //Dopo la lettura, la lista che possiedo combacia con quella in DB
                //readingVM.readingUpdated(false)
            }
        }
        readingVM.changedReadingList.observe(viewLifecycleOwner, readingListStateObserver)

        val isAccessingDatabaseObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                loadingDialog.showNow(childFragmentManager, null)
            }
            else {
                /*
                https://stackoverflow.com/questions/11201022/how-to-correctly-dismiss-a-dialogfragment
                Dopo dismiss devo ricreare subito il Fragment, altrimenti rischio di invocare la show su un fragment che non esiste più : vedi appunti
                 */
                     if (loadingDialog.isAdded) {
                        loadingDialog.dismiss()
                        loadingDialog = LoadingDialogFragment()
                    }
            }
        }
        readingVM.isAccessingDatabase.observe(viewLifecycleOwner, isAccessingDatabaseObserver)

        val showedBookObserver = Observer<Book?> { newShowBook ->
            if (newShowBook != null) {
                if(newShowBook.coverName != "") Picasso.get().load(newShowBook.coverName).placeholder(R.drawable.book_cover_place_holder).error(R.drawable.cover_not_found).into(binding.readingCoverImageView)
                else Picasso.get().load(R.drawable.book_cover_place_holder).into(binding.readingCoverImageView)
                binding.readingTitle.text = newShowBook.title
            }
            else {
                Picasso.get().load(R.drawable.book_cover_place_holder).into(binding.readingCoverImageView)
                binding.readingTitle.text = ""
            }
            showBookInfo = newShowBook
            startUI()
        }
        readingVM.currentShowBook.observe(viewLifecycleOwner, showedBookObserver)

        val markedAsEndedObserver = Observer<Boolean> { marked ->
            if (marked) {
                endedVM.setEndedListChanged(true)
                readingVM.changeNotified()
            }
        }
        readingVM.markedAsEnded.observe(viewLifecycleOwner, markedAsEndedObserver)

    }


    private fun startUI() {

        if (showBookInfo == null) {
            binding.prevBookButton.isEnabled = false
            binding.nextBookButton.isEnabled = false
        }
        else {
            binding.nextBookButton.isEnabled = true
            binding.prevBookButton.isEnabled = true
        }
    }


    override fun onClick(view: View?) {

        when (view) {
            binding.nextBookButton -> { this.readingVM.getNextBook() }

            binding.prevBookButton -> { this.readingVM.getPrevBook() }


            binding.readingTakeNoteBtn -> {
                TODO("Creazione Note")
            }
        }
    }


    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (showBookInfo != null) {
            val inflater: MenuInflater = MenuInflater(requireContext())
            inflater.inflate(R.menu.reading_book_context_menu, menu)
        }
        else {
            val newSnackbar = Snackbar.make(requireView(), getString(R.string.reading_empty_list), Snackbar.LENGTH_SHORT)
            newSnackbar.setAnchorView(R.id.bottomNavigationView)
            newSnackbar.show()
        }
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.readingContextMenuTerminateItem -> {
                val minDate = showBookInfo!!.startDate
                val args = bundleOf("minDay" to minDate!!.startDay, "minMonth" to minDate.startMonth, "minYear" to minDate.startYear)
                val endBookDialog = EndBookDialogFragment()
                endBookDialog.arguments = args
                endBookDialog.show(childFragmentManager, "End Book")
                true
            }

            R.id.readingContextMenuDetailItem -> {
                val detailBookTitle = showBookInfo!!.title
                val detailBookAuthor = showBookInfo!!.author
                val detailBookTime = showBookInfo!!.readTime
                val action = ReadingFragmentDirections.actionReadingFragmentToDetailReadingFragment(detailBookTitle, detailBookAuthor, detailBookTime)
                navController.navigate(action)
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

}