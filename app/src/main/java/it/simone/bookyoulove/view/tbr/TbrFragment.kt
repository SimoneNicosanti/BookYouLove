package it.simone.bookyoulove.view.tbr

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import it.simone.bookyoulove.R
import it.simone.bookyoulove.adapter.TbrAdapter
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.databinding.FragmentTbrBinding
import it.simone.bookyoulove.view.dialog.ConfirmDeleteDialogFragment
import it.simone.bookyoulove.view.setViewEnable
import it.simone.bookyoulove.viewmodel.BookListViewModel


class TbrFragment : Fragment(), TbrAdapter.OnTbrItemClickedListener, SearchView.OnQueryTextListener {

    private lateinit var binding : FragmentTbrBinding

    private val tbrVM : BookListViewModel by viewModels()

    private var searchField : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) searchField = savedInstanceState.getString("searchField").toString()

        requireActivity().invalidateOptionsMenu()
        tbrVM.getTbrList()
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTbrBinding.inflate(inflater, container, false)
        setViewEnable(true, requireActivity())

        setObservers()

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Book>("newTbrBookKey")?.observe(viewLifecycleOwner) {
            tbrVM.notifyNewArrayItem(it)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Book>("newTbrBookKey")
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Book>("modifyTbrBookKey")?.observe(viewLifecycleOwner) {
            tbrVM.notifyArrayItemChanged(it)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Book>("modifyTbrBookKey")
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("startedTbrBookKey")?.observe(viewLifecycleOwner) {
            if (it) {
                //Iniziare un libro equivale a rimuoverlo dalla lista
                tbrVM.notifyArrayItemDelete(false)
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("startedTbrBookKey")
            }
        }

        childFragmentManager.setFragmentResultListener("deleteKey", viewLifecycleOwner) { _, bundle ->
            if (bundle.getBoolean("deleteConfirm")) {
                tbrVM.notifyArrayItemDelete(true)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setObservers() {

        val currentTbrArrayObserver = Observer<MutableList<ShowedBookInfo>> {
            binding.tbrListRecyclerView.adapter = TbrAdapter(it, this)
        }
        tbrVM.currentBookList.observe(viewLifecycleOwner, currentTbrArrayObserver)

        val isAccessingObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                setViewEnable(false, requireActivity())
                binding.tbrLoading.root.visibility = View.VISIBLE
            }

            else {
                setViewEnable(true, requireActivity())
                binding.tbrLoading.root.visibility = View.GONE
            }
        }
        tbrVM.isAccessing.observe(viewLifecycleOwner, isAccessingObserver)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tbr_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return if (item.itemId == R.id.tbrFragmentMenuAddItem) {
            findNavController().navigate(TbrFragmentDirections.actionTbrFragmentToTbrModifyFragment(null))
            true
        }

        else super.onOptionsItemSelected(item)
    }


    override fun onTbrListItemToolbarMenuClicked(position: Int, item: MenuItem?) : Boolean{

        val selectedItem = (binding.tbrListRecyclerView.adapter as TbrAdapter).tbrSet[position]
        tbrVM.changeSelectedItem(selectedItem)

        return when (item?.itemId) {
            R.id.tbrItemMenuEditItem -> {
                findNavController().navigate(TbrFragmentDirections.actionTbrFragmentToTbrModifyFragment(selectedItem.copy()))
                true
            }

            R.id.tbrItemMenuDeleteItem -> {
                val args = bundleOf("itemToDelete" to getString(R.string.delete_book_dialog_title))
                val deleteFragment = ConfirmDeleteDialogFragment()
                deleteFragment.arguments = args
                deleteFragment.show(childFragmentManager, "Delete Confirm")
                true
            }

            R.id.tbrItemMenuStartItem -> {
                findNavController().navigate(TbrFragmentDirections.actionTbrFragmentToStartingFragment(selectedItem.bookId))
                true
            }
            else -> false
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val searchView = menu.findItem(R.id.tbrFragmentMenuSearchItem).actionView as SearchView

        //Mi serve a pulire la SearchView da eventuali ricerce residue in altri fragments
        //searchView.setQuery("", false)

        searchView.setOnQueryTextListener(this)

        if (searchField == "") {
            searchView.isIconified = true
            searchView.clearFocus()
        }
        else {
            searchView.isIconified = false
            searchView.setQuery(searchField, false)
        }

    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        Log.d("Nicosanti", "Text Changed")
        searchField = newText ?: ""
        //Mi assicuro che l'adapter sia valido prima di filtrare: Questo garantisce che non si invochi ricerca su residui di ricerche precedenti
        binding.tbrListRecyclerView.adapter?.let {
            it as TbrAdapter
            it.filter.filter(newText)
        }

        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("searchField", searchField)
    }
}