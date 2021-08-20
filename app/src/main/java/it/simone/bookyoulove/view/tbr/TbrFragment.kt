package it.simone.bookyoulove.view.tbr

import android.os.Bundle
import android.view.*
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
import it.simone.bookyoulove.viewmodel.TbrViewModel


class TbrFragment : Fragment(), TbrAdapter.OnTbrItemClickedListener {

    private lateinit var binding : FragmentTbrBinding

    private var tbrShowedBookInfoArray = arrayOf<ShowedBookInfo>()

    private val tbrVM : TbrViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        childFragmentManager.setFragmentResultListener("deleteKey", viewLifecycleOwner) { _, bundle ->
            if (bundle.getBoolean("deleteConfirm")) {
                tbrVM.deleteTbrBook()
            }
        }
        return binding.root
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
                val args = bundleOf("itemToDelete" to getString(R.string.book_string))
                val deleteFragment = ConfirmDeleteDialogFragment()
                deleteFragment.arguments = args
                deleteFragment.show(childFragmentManager, "Delete Confirm")
                true
            }
            else -> false
        }

    }
}