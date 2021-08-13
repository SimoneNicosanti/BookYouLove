package it.simone.bookyoulove.view.quotes

import android.app.SearchManager
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import it.simone.bookyoulove.R
import it.simone.bookyoulove.adapter.QuoteListAdapter
import it.simone.bookyoulove.database.DAO.ShowQuoteInfo
import it.simone.bookyoulove.databinding.FragmentQuoteListBinding
import it.simone.bookyoulove.view.QUOTE_LIST_ENDED_CALLER
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.QuoteListViewModel


class QuoteListFragment : Fragment(), QuoteListAdapter.OnQuoteListHolderClick, SearchView.OnQueryTextListener {

    private lateinit var binding : FragmentQuoteListBinding

    private val args : QuoteListFragmentArgs by navArgs()

    private val quoteListVM : QuoteListViewModel by viewModels()

    private var loadingDialog = LoadingDialogFragment()

    private lateinit var quoteArray : Array<ShowQuoteInfo>

    private var searchField : String = ""

    private var quoteListFragmentMenu : Menu? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (args.bookKeyTitle == null) {
            quoteListVM.getAllQuotes()
        }
        else {
            quoteListVM.getQuotesByBook(args.bookKeyTitle!!, args.bookKeyAuthor!!, args.bookReadTime)
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentQuoteListBinding.inflate(inflater, container, false)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("quoteDeletedKey")?.observe(viewLifecycleOwner) {
            if (it) {
                quoteListVM.onQuoteDeleted()
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("quoteDeletedKey")
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<ShowQuoteInfo>("modifiedQuoteInfo")?.observe(viewLifecycleOwner) {
            quoteListVM.onModifiedQuote(it)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<ShowQuoteInfo>("modifiedQuoteInfo")
        }

        setObservers()
        return binding.root
    }

    private fun setObservers() {
        val currentQuotesArrayObserver = Observer<Array<ShowQuoteInfo>> { newQuotesArray ->
            binding.quotesListRecyclerView.adapter = QuoteListAdapter(newQuotesArray, this)
            quoteArray = newQuotesArray
        }
        quoteListVM.currentQuotesArray.observe(viewLifecycleOwner, currentQuotesArrayObserver)

        val isAccessingDatabaseObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                loadingDialog.showNow(childFragmentManager, "Loading Fragment")
            } else {
                if (loadingDialog.isAdded) {
                    loadingDialog.dismiss()
                    loadingDialog = LoadingDialogFragment()
                }
            }
        }
        quoteListVM.isAccessingDatabase.observe(viewLifecycleOwner, isAccessingDatabaseObserver)

        val searchFieldObserver = Observer<String> { currentSearchField ->
            searchField = currentSearchField
        }
        quoteListVM.currentSearchField.observe(viewLifecycleOwner, searchFieldObserver)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.quotes_list_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        quoteListFragmentMenu = menu

        val mySearchView = menu.findItem(R.id.quotesListMenuSearchItem).actionView as SearchView
        mySearchView.setOnQueryTextListener(this)

        if (searchField != "") {
            mySearchView.isIconified = false
            mySearchView.setQuery(searchField, false)
        }
        else {
            mySearchView.isIconified = true
        }

        if (args.quoteListCaller == QUOTE_LIST_ENDED_CALLER) {
            val addQuoteItem = menu.findItem(R.id.quoteListMenuAddQuoteItem)
            addQuoteItem.isEnabled = true
            addQuoteItem.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.quoteListMenuAddQuoteItem) {
            /*
                L'unico caso in cui è abilitato è quando sto visualizzando la lista per un libro terminato: se è un libro terminato allora sono dati in input le chiavi e il readTime : posso
                passarle come parametro della action che mi porta alla modifyQuote
             */
            findNavController().navigate(QuoteListFragmentDirections.actionGlobalModifyQuoteFragment(args.bookKeyTitle, args.bookKeyAuthor, args.bookReadTime, null))
            //TODO("Finisci")
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onQuoteListHolderClickedListener(view: View, position: Int) {

        quoteListVM.setCurrentPosition(position)
        findNavController().navigate(QuoteListFragmentDirections.actionQuoteListFragmentToQuoteDetailFragment(
                quoteArray[position].quoteText,
                quoteArray[position].bookTitle,
                quoteArray[position].bookAuthor,
                quoteArray[position].readTime))
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        /*
            Non mi serve metterlo : Quando cambia configurazione l'array ricevuto è già quello filtrato,
            quindi non ho bisogno di fare la submit quando
         */
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        quoteListVM.searchByContents(newText)
        return true
    }

}