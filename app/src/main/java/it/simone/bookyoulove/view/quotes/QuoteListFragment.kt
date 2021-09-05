package it.simone.bookyoulove.view.quotes


import android.content.pm.ActivityInfo
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
import it.simone.bookyoulove.database.entity.Quote
import it.simone.bookyoulove.databinding.FragmentQuoteListBinding
import it.simone.bookyoulove.view.setViewEnable
import it.simone.bookyoulove.viewmodel.quotes.QuoteListViewModel


class QuoteListFragment : Fragment(), QuoteListAdapter.OnQuoteListHolderClick, SearchView.OnQueryTextListener {

    private lateinit var binding : FragmentQuoteListBinding

    private val args : QuoteListFragmentArgs by navArgs()

    private val quoteListVM : QuoteListViewModel by viewModels()

    private var searchField : String = ""



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
        setViewEnable(true, requireActivity())

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("quoteDeletedKey")?.observe(viewLifecycleOwner) {
            if (it) {
                quoteListVM.onQuoteDeleted()
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("quoteDeletedKey")
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Quote>("modifiedQuoteInfo")?.observe(viewLifecycleOwner) {
            quoteListVM.onModifiedQuote(it)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Quote>("modifiedQuoteInfo")
        }

        setObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setObservers() {
        val currentQuotesArrayObserver = Observer<MutableList<ShowQuoteInfo>> { newQuotesArray ->
            binding.quotesListRecyclerView.adapter = QuoteListAdapter(newQuotesArray, this)
        }
        quoteListVM.currentQuotesArray.observe(viewLifecycleOwner, currentQuotesArrayObserver)

        val isAccessingDatabaseObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                setViewEnable(false, requireActivity())
                binding.quoteListLoading.root.visibility = View.VISIBLE
            }

            else {
                setViewEnable(true, requireActivity())
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

        val mySearchView = menu.findItem(R.id.quotesListMenuSearchItem).actionView as SearchView

        mySearchView.setOnQueryTextListener(this)

        if (searchField != "") {
            mySearchView.isIconified = false
            mySearchView.setQuery(searchField, false)
        }
        else {
            mySearchView.isIconified = true
        }

    }


    override fun onQuoteListHolderClickedListener(view: View, position: Int) {
        val selectedQuote = (binding.quotesListRecyclerView.adapter as QuoteListAdapter).quoteSet[position]
        quoteListVM.changeSelectedQuote(selectedQuote)
        findNavController().navigate(QuoteListFragmentDirections.actionQuoteListFragmentToQuoteDetailFragment(
                selectedQuote.quoteId,
                selectedQuote.bookId))
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        searchField = newText ?: ""
        binding.quotesListRecyclerView.adapter?.let {
            it as QuoteListAdapter
            it.filter.filter(newText)
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("searchField", searchField)
    }
}