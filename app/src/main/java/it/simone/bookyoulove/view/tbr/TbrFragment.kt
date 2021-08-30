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
import it.simone.bookyoulove.viewmodel.tbr.TbrViewModel


class TbrFragment : Fragment(), TbrAdapter.OnTbrItemClickedListener, SearchView.OnQueryTextListener {

    private lateinit var binding : FragmentTbrBinding

    private var tbrShowedBookInfoArray = arrayOf<ShowedBookInfo>()

    private val tbrVM : TbrViewModel by viewModels()

    private lateinit var searchView : SearchView

    private var searchField : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) searchField = savedInstanceState.getString("searchField").toString()

        tbrVM.getTbrArray()
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTbrBinding.inflate(inflater, container, false)

        setObservers()

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Book>("newTbrBookKey")?.observe(viewLifecycleOwner) {
            tbrVM.onNewTbrBookAdded(it)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Book>("newTbrBookKey")
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Book>("modifyTbrBookKey")?.observe(viewLifecycleOwner) {
            tbrVM.onModifyTbrBook(it)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Book>("modifyTbrBookKey")
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("startedTbrBookKey")?.observe(viewLifecycleOwner) {
            if (it) {
                tbrVM.onStartedBook()
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("startedTbrBookKey")
            }
        }

        childFragmentManager.setFragmentResultListener("deleteKey", viewLifecycleOwner) { _, bundle ->
            if (bundle.getBoolean("deleteConfirm")) {
                tbrVM.deleteTbrBook()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setObservers() {

        val currentTbrArrayObserver = Observer<Array<ShowedBookInfo>> {
            binding.tbrListRecyclerView.adapter = TbrAdapter(it, this)
            tbrShowedBookInfoArray = it
        }
        tbrVM.currentTbrArray.observe(viewLifecycleOwner, currentTbrArrayObserver)

        val isAccessingObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.tbrLoading.root.visibility = View.VISIBLE
            }

            else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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

        tbrVM.setItemPosition(position)

        return when (item?.itemId) {
            R.id.tbrItemMenuEditItem -> {
                findNavController().navigate(TbrFragmentDirections.actionTbrFragmentToTbrModifyFragment(tbrShowedBookInfoArray[position].copy()))
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
                findNavController().navigate(TbrFragmentDirections.actionTbrFragmentToStartingFragment(tbrShowedBookInfoArray[position].bookId))
                true
            }
            else -> false
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        searchView = menu.findItem(R.id.tbrFragmentMenuSearchItem).actionView as SearchView


        if (searchField == "") {
            searchView.isIconified = true
        }
        else {
            searchView.isIconified = false
            searchView.setQuery(searchField, false)
        }
        //Imposto il listener dopo aver restaurato lo stato della searchView: in questo modo non triggero la onQueryTextChange che invoca il filtro
        searchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        Log.d("Nicosanti", "Text Changed")
        searchField = newText ?: ""
        tbrVM.onSearchQuery(newText)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("searchField", searchField)
    }
}