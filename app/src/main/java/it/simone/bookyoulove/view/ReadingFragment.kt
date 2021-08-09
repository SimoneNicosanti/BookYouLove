package it.simone.bookyoulove.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.encoders.annotations.Encodable
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.adapter.ReadingAdapter
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.databinding.FragmentReadingBinding
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.EndedViewModel
import it.simone.bookyoulove.viewmodel.ReadingViewModel


//TODO("Inserire meccanismo di cancelazione del libro")

//https://medium.com/holler-developers/paging-image-gallery-with-recyclerview-f059d035b7e7
//https://medium.com/@supahsoftware/custom-android-views-carousel-recyclerview-7b9318d23e9a

class ReadingFragment : Fragment() , ReadingAdapter.OnReadingItemMenuItemClickListener, View.OnClickListener {

    private lateinit var binding: FragmentReadingBinding

    private lateinit var navController: NavController

    //Variabile che mantiene le informazioni del libro da mostrare
    //private var showBookInfo : ShowedBookInfo? = null

    private var loadingDialog = LoadingDialogFragment()

    private val readingVM: ReadingViewModel by activityViewModels()
    private val endedVM: EndedViewModel by activityViewModels()

    private var bookArray : Array<ShowedBookInfo> = arrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //readingVM.loadReadingBookList()
        setHasOptionsMenu(true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentReadingBinding.inflate(inflater, container, false)

        //registerForContextMenu(binding.readingCoverImageView)

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
                readingVM.loadReadingBookList()
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

        /*
        val showedBookObserver = Observer<ShowedBookInfo?> { newShowBook ->
            if (newShowBook != null) {
                if(newShowBook.coverName != "") Picasso.get().load(newShowBook.coverName).placeholder(R.drawable.book_cover_place_holder).error(R.drawable.cover_not_found).into(binding.readingCoverImageView)
                else Picasso.get().load(R.drawable.book_cover_place_holder).into(binding.readingCoverImageView)
                //binding.readingTitle.text = newShowBook.title
            }
            else {
                Picasso.get().load(R.drawable.book_cover_place_holder).into(binding.readingCoverImageView)
                //binding.readingTitle.text = ""
            }
            showBookInfo = newShowBook
            //startUI()
        }
        readingVM.currentShowBook.observe(viewLifecycleOwner, showedBookObserver)*/

        val currentListObserver = Observer<Array<ShowedBookInfo>> { newArray ->
            if (!newArray.isEmpty()) {
                binding.cardSlider.adapter = ReadingAdapter(newArray, this)
            }

            else {
                val placeholderArray = arrayOf(ShowedBookInfo("Begin Read", "", 0, "", null, null, null))
                binding.cardSlider.adapter = ReadingAdapter(placeholderArray, this)
            }

            bookArray = newArray
        }
        readingVM.currentReadingBookArray.observe(viewLifecycleOwner, currentListObserver)

    }



    override fun onClick(view: View?) {

    }

    /*
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (showBookInfo != null) {
            val inflater = MenuInflater(requireContext())
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
    }*/


    override fun onReadingItemMenuItemClickListener(position: Int, item: MenuItem?): Boolean {

        if (bookArray.isEmpty()) {
            val newSnackbar = Snackbar.make(requireView(), getString(R.string.reading_empty_list), Snackbar.LENGTH_SHORT)
            newSnackbar.setAnchorView(R.id.bottomNavigationView)
            newSnackbar.show()
            return true
        }

        //Dico al VM l'indice nella lista dell'elemento selezionato
        readingVM.setCurrentItemPosition(position)

        return when (item?.itemId) {

            R.id.readingContextMenuTakeNoteItem -> {
                findNavController().navigate(ReadingFragmentDirections.actionGlobalModifyQuoteFragment(bookArray[position].title, bookArray[position].author))
                true
            }

            R.id.readingContextMenuDetailItem -> {
                val detailBookTitle = bookArray[position].title
                val detailBookAuthor = bookArray[position].author
                val detailBookTime = bookArray[position].readTime
                val action = ReadingFragmentDirections.actionReadingFragmentToDetailReadingFragment(detailBookTitle, detailBookAuthor, detailBookTime)
                navController.navigate(action)
                true
            }

            R.id.readingContextMenuTerminateItem -> {
                val navController = findNavController()
                val action = ReadingFragmentDirections.actionReadingFragmentToEndingFragment(bookArray[position].title, bookArray[position].author, bookArray[position].readTime)
                navController.navigate(action)
                true
            }

            R.id.readingContextMenuAbandonItem -> {
                true
                TODO("Abandont --> TBR / Delete")
            }
            else -> super.onOptionsItemSelected(item!!)
        }
    }

}
