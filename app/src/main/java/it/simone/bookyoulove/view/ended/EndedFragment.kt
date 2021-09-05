package it.simone.bookyoulove.view.ended

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import it.simone.bookyoulove.R
import it.simone.bookyoulove.adapter.EndedAdapter
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.databinding.FragmentEndedBinding
import it.simone.bookyoulove.view.*
import it.simone.bookyoulove.viewmodel.BookListViewModel


class EndedFragment : Fragment(), EndedAdapter.OnRecyclerViewItemSelectedListener, SearchView.OnQueryTextListener, AdapterView.OnItemSelectedListener {

    private lateinit var binding: FragmentEndedBinding

    private val endedVM : BookListViewModel by viewModels()

    private var mySearchView : SearchView? = null

    private var searchField = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            searchField = savedInstanceState.getString("searchField")!!
        }

        endedVM.getEndedList()

        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEndedBinding.inflate(inflater, container, false)

        setViewEnable(true, requireActivity())

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,
                arrayOf(getString(R.string.title_string),
                        getString(R.string.author_string),
                        getString(R.string.your_rate_string),
                        getString(R.string.year_string)))
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.endedFragmentSearchBySpinner.adapter = spinnerAdapter

        binding.endedFragmentSearchBySpinner.isEnabled = (searchField == "")

        binding.endedFragmentSearchBySpinner.onItemSelectedListener = this

        setObservers()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("deleteKey")?.observe(viewLifecycleOwner) {
            if (it) {
                endedVM.notifyArrayItemDelete(false)
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("deleteKey")
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Book>("endedModifiedBookKey")?.observe(viewLifecycleOwner) {
            endedVM.notifyArrayItemChanged(it)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Book>("endedModifiedBookKey")
        }
    }


    private fun setObservers() {
        val isAccessingDatabaseObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                setViewEnable(false, requireActivity())
                binding.endedLoading.root.visibility = View.VISIBLE
            }

            else {
                setViewEnable(true, requireActivity())
                binding.endedLoading.root.visibility = View.GONE
            }
        }
        endedVM.isAccessing.observe(viewLifecycleOwner, isAccessingDatabaseObserver)

        val currentReadListObserver = Observer<MutableList<ShowedBookInfo>> {

            val linearIndicator = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("endedLinearLayout", false)
            if (linearIndicator) {
                binding.endedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
            else {
                binding.endedRecyclerView.layoutManager = GridLayoutManager(requireContext(), resources.getInteger(R.integer.ended_grid_row_item_count_portr))
            }

            binding.endedRecyclerView.adapter = EndedAdapter(it, this, linearIndicator)

        }
        endedVM.currentBookList.observe(viewLifecycleOwner, currentReadListObserver)

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.ended_fragment_menu, menu)
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        mySearchView = menu.findItem(R.id.endedMenuSearchItem).actionView as SearchView

        //mySearchView?.setQuery("", false)

        mySearchView?.setOnQueryTextListener(this)

        if (searchField == "") {
            mySearchView?.isIconified = true
        }
        else {
            mySearchView?.isIconified = false
            mySearchView?.setQuery(searchField, false)
        }

    }


    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }


    override fun onQueryTextChange(newText: String?): Boolean {
        binding.endedFragmentSearchBySpinner.isEnabled = (newText == null || newText == "")
        val filterType : Int = binding.endedFragmentSearchBySpinner.selectedItemPosition

        //Mi assicuro che non ci siano chiamate per residui di ricerca precedenti
        binding.endedRecyclerView.adapter?.let {
            it as EndedAdapter
            it.filterType = filterType

            searchField = newText ?: ""

            if (filterType == SEARCH_BY_YEAR || filterType == SEARCH_BY_RATE) {
                //evito overflow nel confronto se si inserisce un numero eccessivamente grande
                if (newText?.length!! <= 4) {
                    //endedVM.filterArray(newText, filterType)
                    it.filter.filter(newText)
                }
            } else {
                it.filter.filter(newText)
            }
        }
        return true
    }


    override fun onRecyclerViewItemSelected(position: Int) {
        val shownBookInfo = (binding.endedRecyclerView.adapter as EndedAdapter).bookSet[position]
        endedVM.changeSelectedItem(shownBookInfo)

        Log.d(TAG, "Position $position")
        findNavController().navigate(EndedFragmentDirections.actionEndedFragmentToEndedDetailFragment(shownBookInfo.bookId))
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("searchField", searchField)
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            SEARCH_BY_TITLE, SEARCH_BY_AUTHOR -> mySearchView?.inputType = InputType.TYPE_CLASS_TEXT
            SEARCH_BY_RATE, SEARCH_BY_YEAR -> mySearchView?.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }


    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

}

