package it.simone.bookyoulove.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.simone.bookyoulove.R
import it.simone.bookyoulove.adapter.EndedAdapter
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.databinding.FragmentEndedBinding
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.EndedViewModel


class EndedFragment : Fragment() {

    private lateinit var binding: FragmentEndedBinding

    private val endedVM : EndedViewModel by viewModels()


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
            if (it) LoadingDialogFragment().show(childFragmentManager, "Loading Fragment")
        }
        endedVM.isAccessingDatabase.observe(viewLifecycleOwner, isAccessingDatabaseObserver)

        val currentReadListObserver = Observer<Array<Book>> {
            //In base ad orientameno del dispositivo cambio il numero di elementi mostrati su una riga, nel caso si stia usando una griglia
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                binding.readRecyclerView.layoutManager = GridLayoutManager(requireContext(), resources.getInteger(R.integer.ended_grid_row_item_count_portr))
                binding.readRecyclerView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            else {
                binding.readRecyclerView.layoutManager = GridLayoutManager(requireContext(), resources.getInteger((R.integer.ended_grid_row_item_count_land)))
                binding.readRecyclerView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            
            binding.readRecyclerView.adapter = EndedAdapter(it)
        }
        endedVM.currentReadList.observe(viewLifecycleOwner, currentReadListObserver)

        val changedEndedListObserver = Observer<Boolean> { isChanged ->
            if (isChanged) endedVM.getReadList()
        }
        endedVM.changedEndedList.observe(viewLifecycleOwner, changedEndedListObserver)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.ended_fragment_menu, menu)

    }
}