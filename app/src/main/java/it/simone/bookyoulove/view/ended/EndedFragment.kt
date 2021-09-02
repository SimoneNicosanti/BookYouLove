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
import androidx.fragment.app.activityViewModels
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
import it.simone.bookyoulove.viewmodel.ended.EndedViewModel


class EndedFragment : Fragment(), EndedAdapter.OnRecyclerViewItemSelectedListener, SearchView.OnQueryTextListener, AdapterView.OnItemSelectedListener {

    private lateinit var binding: FragmentEndedBinding

    private val endedVM : EndedViewModel by viewModels()
    private lateinit var endedBookArray : Array<ShowedBookInfo>

    private var mySearchView : SearchView? = null

    private var searchField = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            searchField = savedInstanceState.getString("searchField")!!
        }
        else {
            endedVM.resetSearchField()
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
                endedVM.notifyArrayItemDelete()
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
                setViewEnable(false, requireActivity(), )
                binding.endedLoading.root.visibility = View.VISIBLE
            }

            else {
                setViewEnable(true, requireActivity(), )
                binding.endedLoading.root.visibility = View.GONE
            }
        }
        endedVM.isAccessingDatabase.observe(viewLifecycleOwner, isAccessingDatabaseObserver)

        val currentReadListObserver = Observer<Array<ShowedBookInfo>> {

            val linearIndicator = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("endedLinearLayout", false)
            if (linearIndicator) {
                binding.endedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
            else {
                binding.endedRecyclerView.layoutManager = GridLayoutManager(requireContext(), resources.getInteger(R.integer.ended_grid_row_item_count_portr))
            }

            endedBookArray = it

            binding.endedRecyclerView.adapter = EndedAdapter(it, this, linearIndicator)

        }
        endedVM.currentReadList.observe(viewLifecycleOwner, currentReadListObserver)

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.ended_fragment_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        mySearchView = menu.findItem(R.id.endedMenuSearchItem).actionView as SearchView

        if (searchField == "") {
            mySearchView!!.isIconified = true
        }
        else {
            mySearchView!!.isIconified = false
            mySearchView!!.setQuery(searchField, false)
        }

        mySearchView?.setOnQueryTextListener(this)
    }


    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        Log.d("Nicosanti", "Query Text Changed $newText")
        binding.endedFragmentSearchBySpinner.isEnabled = (newText == "")
        val filterType : Int = binding.endedFragmentSearchBySpinner.selectedItemPosition
        searchField = newText ?: ""
        if (filterType == SEARCH_BY_YEAR || filterType == SEARCH_BY_RATE) {
            //evito overflow nel confronto se si inserisce un numero eccessivamente grande
            if (newText?.length!! <= 4) endedVM.filterArray(newText, filterType)
        }
        else {
            endedVM.filterArray(newText, filterType)
        }
        return true
    }

    override fun onRecyclerViewItemSelected(position: Int) {
        val selectedBook : ShowedBookInfo = endedBookArray[position]
        endedVM.currentSelectedPosition = position
        val navController = findNavController()
        val action = EndedFragmentDirections.actionEndedFragmentToEndedDetailFragment(selectedBook.bookId)
        navController.navigate(action)
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

