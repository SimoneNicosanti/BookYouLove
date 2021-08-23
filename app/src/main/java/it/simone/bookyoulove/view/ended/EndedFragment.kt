package it.simone.bookyoulove.view.ended

import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import it.simone.bookyoulove.R
import it.simone.bookyoulove.adapter.EndedAdapter
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.databinding.FragmentEndedBinding
import it.simone.bookyoulove.view.SEARCH_BY_AUTHOR
import it.simone.bookyoulove.view.SEARCH_BY_RATE
import it.simone.bookyoulove.view.SEARCH_BY_TITLE
import it.simone.bookyoulove.view.SEARCH_BY_YEAR
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.EndedViewModel


class EndedFragment : Fragment(), EndedAdapter.OnRecyclerViewItemSelectedListener, SearchView.OnQueryTextListener, AdapterView.OnItemSelectedListener {

    private lateinit var binding: FragmentEndedBinding

    private val endedVM : EndedViewModel by activityViewModels()
    private lateinit var endedBookArray : Array<ShowedBookInfo>

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

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,
                arrayOf(getString(R.string.title_string),
                        getString(R.string.author_string),
                        getString(R.string.your_rate_string),
                        getString(R.string.year_string)))
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.endedFragmentSearchBySpinner.adapter = spinnerAdapter

        binding.endedFragmentSearchBySpinner.onItemSelectedListener = this

        setObservers()
        return binding.root
    }


    private fun setObservers() {
        val isAccessingDatabaseObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.endedLoading.root.visibility = View.VISIBLE
            }

            else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.endedLoading.root.visibility = View.GONE
            }
        }
        endedVM.isAccessingDatabase.observe(viewLifecycleOwner, isAccessingDatabaseObserver)

        val currentReadListObserver = Observer<Array<ShowedBookInfo>> {
            //In base ad orientameno del dispositivo cambio il numero di elementi mostrati su una riga, nel caso si stia usando una griglia

            val linearIndicator = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("endedLinearLayout", false)
            if (linearIndicator) {
                binding.endedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            } else {
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    binding.endedRecyclerView.layoutManager = GridLayoutManager(requireContext(), resources.getInteger(R.integer.ended_grid_row_item_count_portr))
                    //binding.endedRecyclerView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
                } else {
                    binding.endedRecyclerView.layoutManager = GridLayoutManager(requireContext(), resources.getInteger((R.integer.ended_grid_row_item_count_land)))
                    //binding.endedRecyclerView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
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
        endedVM.filterArray(newText, filterType)
        return true
    }

    override fun onRecyclerViewItemSelected(position: Int) {
        val selectedBook : ShowedBookInfo = endedBookArray[position]
        //endedVM.setSelectedBook(selectedBook)
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


    override fun onDetach() {
        super.onDetach()
        endedVM.resetSearchParams()
    }
}

