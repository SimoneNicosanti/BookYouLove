package it.simone.bookyoulove.view.quotes


import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import it.simone.bookyoulove.R
import it.simone.bookyoulove.adapter.QuoteListAdapter
import it.simone.bookyoulove.database.DAO.ShowQuoteInfo
import it.simone.bookyoulove.databinding.FragmentQuoteListBinding
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.QuoteListViewModel


class QuoteListFragment : Fragment(), QuoteListAdapter.OnQuoteListHolderClick, SearchView.OnQueryTextListener {

    private lateinit var binding : FragmentQuoteListBinding

    private val args : QuoteListFragmentArgs by navArgs()

    private val quoteListVM : QuoteListViewModel by viewModels()

    private lateinit var quoteArray : Array<ShowQuoteInfo>

    private var searchField : String = ""

    private var quoteListFragmentMenu : Menu? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (args.bookId == 0L) {
            quoteListVM.getAllQuotes()
        }
        else {
            quoteListVM.getQuotesByBookId(args.bookId)
        }

        if (savedInstanceState != null) searchField = savedInstanceState.getString("searchField").toString()

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
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.quoteListLoading.root.visibility = View.VISIBLE
            }

            else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.quoteListLoading.root.visibility = View.GONE
            }
        }
        quoteListVM.isAccessingDatabase.observe(viewLifecycleOwner, isAccessingDatabaseObserver)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.quotes_list_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        quoteListFragmentMenu = menu

        val mySearchView = menu.findItem(R.id.quotesListMenuSearchItem).actionView as SearchView

        if (searchField != "") {
            mySearchView.isIconified = false
            mySearchView.setQuery(searchField, false)
        }
        else {
            mySearchView.isIconified = true
        }

        mySearchView.setOnQueryTextListener(this)

    }


    override fun onQuoteListHolderClickedListener(view: View, position: Int) {

        quoteListVM.setCurrentPosition(position)
        findNavController().navigate(QuoteListFragmentDirections.actionQuoteListFragmentToQuoteDetailFragment(
                quoteArray[position].quoteId,
                quoteArray[position].bookId))
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        /*
            Non mi serve metterlo : Quando cambia configurazione l'array ricevuto è già quello filtrato,
            quindi non ho bisogno di fare la submit quando
         */
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        searchField = newText!!
        quoteListVM.searchByContents(newText)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("searchField", searchField)
    }
}