package it.simone.bookyoulove.view.ended

import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
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
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.EndedViewModel


class EndedFragment : Fragment(), EndedAdapter.OnRecyclerViewItemSelectedListener, SearchView.OnQueryTextListener {

    private lateinit var binding: FragmentEndedBinding

    private val endedVM : EndedViewModel by activityViewModels()
    private lateinit var endedBookArray : Array<ShowedBookInfo>

    private var loadingDialog = LoadingDialogFragment()

    private var endedFragmentMenu : Menu? = null

    private var searchField = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEndedBinding.inflate(inflater, container, false)

        setObservers()
        return binding.root
    }


    private fun setObservers() {
        val isAccessingDatabaseObserver = Observer<Boolean> {
            if (it) {
                loadingDialog.showNow(childFragmentManager, "Loading Fragment")
            } else {
                if (loadingDialog.isAdded) {
                    loadingDialog.dismiss()
                    loadingDialog = LoadingDialogFragment()
                }
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

        val changedEndedListObserver = Observer<Boolean> { isChanged ->
            if (isChanged) {
                endedVM.getEndedList()
                endedVM.setEndedListChanged(false)
            }
        }
        endedVM.changedEndedList.observe(viewLifecycleOwner, changedEndedListObserver)

        val currentSearchFieldObserver = Observer<String> {
            searchField = it
        }
        endedVM.currentSearchField.observe(viewLifecycleOwner, currentSearchFieldObserver)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.ended_fragment_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        endedFragmentMenu = menu
        val mySearchView = menu.findItem(R.id.endedMenuSearchItem).actionView as SearchView
        mySearchView.setOnQueryTextListener(this)

        if (searchField == "") {
            mySearchView.isIconified = true
        }
        else {
            mySearchView.isIconified = false
            mySearchView.setQuery(searchField, false)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val mySearchView = endedFragmentMenu!!.findItem(R.id.endedMenuSearchItem).actionView as SearchView
        if (searchField != "") {
            //Se cambia il tipo di ricerca resetto la lista a quella originale
            mySearchView.setQuery("", true)

        }

        item.isChecked = !item.isChecked
        when (item.itemId) {
            R.id.endedMenuSearchTypeTitle, R.id.endedMenuSearchTypeAuthor -> {
                mySearchView.inputType = InputType.TYPE_CLASS_TEXT
            }

            R.id.endedMenuSearchTypeRate -> {
                mySearchView.inputType = InputType.TYPE_CLASS_NUMBER
            }
        }
        return true
    }


    override fun onQueryTextSubmit(query: String?): Boolean {
        onQueryTextChange(query)
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val filterType : Int
        filterType = when {
            endedFragmentMenu?.findItem(R.id.endedMenuSearchTypeTitle)!!.isChecked -> SEARCH_BY_TITLE
            endedFragmentMenu?.findItem(R.id.endedMenuSearchTypeAuthor)!!.isChecked -> SEARCH_BY_AUTHOR
            else -> SEARCH_BY_RATE
        }

        endedVM.filterArray(newText, filterType)
        return true
    }


    override fun onRecyclerViewItemSelected(position: Int) {
        val selectedBook : ShowedBookInfo = endedBookArray[position]
        endedVM.setSelectedBook(selectedBook)
        endedVM.currentSelectedPosition = position
        val navController = findNavController()
        val action = EndedFragmentDirections.actionEndedFragmentToEndedDetailFragment(selectedBook.keyTitle, selectedBook.keyAuthor, selectedBook.readTime)
        navController.navigate(action)
    }
}

