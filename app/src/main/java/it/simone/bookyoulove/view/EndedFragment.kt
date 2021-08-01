package it.simone.bookyoulove.view

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
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.databinding.FragmentEndedBinding
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.EndedViewModel


class EndedFragment : Fragment(), EndedAdapter.OnRecyclerViewItemSelectedListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener{

    private lateinit var binding: FragmentEndedBinding

    private val endedVM : EndedViewModel by activityViewModels()
    private lateinit var endedBookArray : Array<Book>

    private var loadingDialog = LoadingDialogFragment()

    private lateinit var fragmentMenu : Menu
    private var isSearching = false
    private lateinit var mySearchView: SearchView


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
            }
            else {
                if (loadingDialog.isAdded) {
                    loadingDialog.dismiss()
                    loadingDialog = LoadingDialogFragment()
                }
            }
        }
        endedVM.isAccessingDatabase.observe(viewLifecycleOwner, isAccessingDatabaseObserver)

        val currentReadListObserver = Observer<Array<Book>> {
            //In base ad orientameno del dispositivo cambio il numero di elementi mostrati su una riga, nel caso si stia usando una griglia

            val linearIndicator = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("endedLinearLayout", false)
            if (linearIndicator) {
                binding.endedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            }

            else {
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    binding.endedRecyclerView.layoutManager = GridLayoutManager(requireContext(), resources.getInteger(R.integer.ended_grid_row_item_count_portr))
                    //binding.endedRecyclerView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
                }
                else {
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

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.ended_fragment_menu, menu)
        mySearchView = menu.findItem(R.id.endedMenuSearchItem).actionView as SearchView
        mySearchView.setOnQueryTextListener(this)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        fragmentMenu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i("Nicosanti", "Options")
        if (!mySearchView.isIconified) {
            //Cancello Query Precedente
            mySearchView.setQuery(null, true)
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

        Snackbar.make(requireView(), "Search Options", Snackbar.LENGTH_SHORT).show()
        return super.onOptionsItemSelected(item)
    }


    override fun onRecyclerViewItemSelected(position: Int) {
        val selectedBook : Book = endedBookArray[position]
        //Toast.makeText(requireContext(), "Selected ${selectedBook.title}", Toast.LENGTH_SHORT).show()
        endedVM.setSelectedBook(selectedBook)
        val navController = findNavController()
        val action = EndedFragmentDirections.actionEndedFragmentToEndedDetailFragment()
        navController.navigate(action)
    }


    override fun onQueryTextSubmit(query: String?): Boolean {
        mySearchView.setQuery(null, true)
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        Log.i("Nicosanti", "Cambiato")
        val filterType : Int
        when {
            fragmentMenu.findItem(R.id.endedMenuSearchTypeTitle).isChecked -> filterType = SEARCH_BY_TITLE
            fragmentMenu.findItem(R.id.endedMenuSearchTypeAuthor).isChecked -> filterType = SEARCH_BY_AUTHOR
            else -> filterType = SEARCH_BY_RATE
        }

        endedVM.filterArray(newText, filterType)
        return true
    }

    override fun onClose(): Boolean {
        mySearchView.setQuery(null, true)
        return true
    }

}

