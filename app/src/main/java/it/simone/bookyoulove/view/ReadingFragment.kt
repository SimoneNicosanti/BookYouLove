package it.simone.bookyoulove.view

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.EndDate
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.databinding.FragmentReadingBinding
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.EndedViewModel
import it.simone.bookyoulove.viewmodel.ReadingViewModel


//TODO("Inserire meccanismo di cancelazione del libro")

class ReadingFragment : Fragment() , View.OnClickListener{

    private lateinit var binding: FragmentReadingBinding

    private lateinit var navController: NavController

    //Variabile che mantiene le informazioni del libro da mostrare
    private var showBookInfo : ShowedBookInfo? = null

    private var loadingDialog = LoadingDialogFragment()

    private val readingVM: ReadingViewModel by activityViewModels()
    private val endedVM: EndedViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentReadingBinding.inflate(inflater, container, false)

        binding.nextBookButton.setOnClickListener(this)
        binding.prevBookButton.setOnClickListener(this)

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
                //Quando creo un nuovo libro, i parametri per detail sono nulli
                val action = ReadingFragmentDirections.actionReadingFragmentToNewReadingBookFragment(null, null, 0)
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

        val showedBookObserver = Observer<ShowedBookInfo?> { newShowBook ->
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
                val navController = findNavController()
                val action = ReadingFragmentDirections.actionReadingFragmentToEndingFragment(showBookInfo!!.title, showBookInfo!!.author, showBookInfo!!.readTime)
                navController.navigate(action)
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