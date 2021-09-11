package it.simone.bookyoulove.view.reading


import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.islamkhsh.CardSliderViewPager
import com.google.android.material.snackbar.Snackbar
import it.simone.bookyoulove.R
import it.simone.bookyoulove.adapter.ReadingAdapter
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.databinding.FragmentReadingBinding
import it.simone.bookyoulove.view.dialog.LeavingReadingDialog
import it.simone.bookyoulove.view.setViewEnable
import it.simone.bookyoulove.viewmodel.BookListViewModel


class ReadingFragment : Fragment() , ReadingAdapter.OnReadingItemMenuItemClickListener {

    private lateinit var binding: FragmentReadingBinding

    private val readingVM: BookListViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentReadingBinding.inflate(inflater, container, false)
        //setViewEnable(true, requireActivity())

        setObservers()

        childFragmentManager.setFragmentResultListener("leavingKey", this) {_, bundle ->
            val moveToTbr = bundle.getBoolean("moveToTbr")

            if (moveToTbr) readingVM.notifyBookMove()
            else readingVM.notifyArrayItemDelete(true)
        }

        readingVM.getReadingList()

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reading_fragment_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.readingNewItem -> {
                //Quando creo un nuovo libro, i parametri per detail sono nulli
                val action = ReadingFragmentDirections.actionReadingFragmentToNewReadingBookFragment(null)
                findNavController().navigate(action)
            }
        }
        return true
    }


    private fun setObservers() {

        val isAccessingDatabaseObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                setViewEnable(false, requireActivity())
                binding.loadingInclude.root.visibility = View.VISIBLE
            }

            else {
                setViewEnable(true, requireActivity())
                binding.loadingInclude.root.visibility = View.GONE
            }
        }
        readingVM.isAccessing.observe(viewLifecycleOwner, isAccessingDatabaseObserver)


        val currentListObserver = Observer<MutableList<ShowedBookInfo>> { newArray ->
            val cardSlider : CardSliderViewPager = view?.findViewById(R.id.cardSlider)!! 
            if (newArray.isNotEmpty()) {
                cardSlider.adapter = ReadingAdapter(newArray, this)
            }

            else {
                val placeholderArray = mutableListOf(ShowedBookInfo(
                    0,
                    title = getString(R.string.begin_read_string),
                    "",
                    "",
                    null,
                    null,
                    null,
                    0))
                cardSlider.adapter = ReadingAdapter(placeholderArray, this)
            }
        }
        readingVM.currentBookList.observe(viewLifecycleOwner, currentListObserver)
    }


    override fun onReadingItemMenuItemClickListener(position: Int, item: MenuItem?): Boolean {
        val cardSlider = requireView().findViewById<CardSliderViewPager>(R.id.cardSlider)
        val selectedItem = (cardSlider.adapter as ReadingAdapter).readingBookSetAll[position]
        if (selectedItem.bookId == 0L) {
            //Significa che c'è il Place Holder
            val newSnackbar = Snackbar.make(requireView(), getString(R.string.reading_empty_list), Snackbar.LENGTH_SHORT)
            newSnackbar.setAnchorView(R.id.bottomNavigationView)
            newSnackbar.show()
            return true
        }

        //Dico al VM l'item della lista che è stato selezionato
        readingVM.changeSelectedItem(selectedItem)

        return when (item?.itemId) {

            R.id.readingContextMenuTakeNoteItem -> {
                findNavController().navigate(ReadingFragmentDirections.actionGlobalModifyQuoteFragment(
                    null,
                    selectedItem.bookId,
                    selectedItem.title,
                    selectedItem.author
                ))
                true
            }

            R.id.readingContextMenuDetailItem -> {

                val action = ReadingFragmentDirections.actionReadingFragmentToDetailReadingFragment(selectedItem.bookId)
                findNavController().navigate(action)
                true
            }

            R.id.readingContextMenuTerminateItem -> {
                val action = ReadingFragmentDirections.actionReadingFragmentToEndingFragment(selectedItem.bookId)
                findNavController().navigate(action)
                true
            }

            R.id.readingContextMenuLeaveItem -> {
                LeavingReadingDialog().show(childFragmentManager, "Leaving Dialog")
                true
            }

            R.id.readingContextMenuQuotesListItem -> {
                findNavController().navigate(ReadingFragmentDirections.actionGlobalQuoteListFragment(selectedItem.bookId))
                true
            }
            else -> super.onOptionsItemSelected(item!!)
        }
    }

}
