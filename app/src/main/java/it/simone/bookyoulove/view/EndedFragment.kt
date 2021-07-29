package it.simone.bookyoulove.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import it.simone.bookyoulove.R
import it.simone.bookyoulove.adapter.EndedAdapter
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.databinding.FragmentEndedBinding
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.EndedViewModel


class EndedFragment : Fragment(), EndedAdapter.OnRecyclerViewItemSelectedListener{

    private lateinit var binding: FragmentEndedBinding

    private val endedVM : EndedViewModel by activityViewModels()
    private lateinit var endedBookArray : Array<Book>


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

        binding.endedRecyclerView.setOnClickListener {
        }
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
                binding.endedRecyclerView.layoutManager = GridLayoutManager(requireContext(), resources.getInteger(R.integer.ended_grid_row_item_count_portr))
                //binding.endedRecyclerView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            else {
                binding.endedRecyclerView.layoutManager = GridLayoutManager(requireContext(), resources.getInteger((R.integer.ended_grid_row_item_count_land)))
                //binding.endedRecyclerView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            endedBookArray = it
            binding.endedRecyclerView.adapter = EndedAdapter(it, this)

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

    override fun onRecyclerViewItemSelected(position: Int) {
        val selectedBook : Book = endedBookArray[position]
        //Toast.makeText(requireContext(), "Selected ${selectedBook.title}", Toast.LENGTH_SHORT).show()
        endedVM.setSelectedBook(selectedBook)
        val navController = findNavController()
        val action = EndedFragmentDirections.actionEndedFragmentToEndedDetailFragment()
        navController.navigate(action)
    }

}