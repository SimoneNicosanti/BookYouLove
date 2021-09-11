package it.simone.bookyoulove.view.reading

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.databinding.FragmentDetailReadingBinding
import it.simone.bookyoulove.utilsClass.DateFormatClass
import it.simone.bookyoulove.utilsClass.MyPicasso
import it.simone.bookyoulove.view.setViewEnable
import it.simone.bookyoulove.viewmodel.DetailBookViewModel


class DetailReadingFragment : Fragment() {


    private lateinit var binding : FragmentDetailReadingBinding

    private val detailReadingVM : DetailBookViewModel by viewModels()

    private val args : DetailReadingFragmentArgs by navArgs()

    private lateinit var detailBook : Book


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        detailReadingVM.loadDetailBook(args.detailReadingBookId)
    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        binding = FragmentDetailReadingBinding.inflate(inflater, container, false)
        //setViewEnable(true, requireActivity())

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Book>("modifiedBook")?.observe(viewLifecycleOwner) {
            detailReadingVM.onBookModified(it)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Book>("modifiedBook")
            //readingVM.notifyReadingBookModified(it)
        }

        setObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setObservers() {

        val currentBookObserver = Observer<Book> { currentBook ->
            Log.d("Nicosanti", "Detail Reading Current")
            MyPicasso().putImageIntoView(currentBook.coverName, binding.detailReadingCoverImageView)

            binding.detailReadingTitle.text = currentBook.title
            binding.detailReadingAuthor.text = currentBook.author

            binding.detailReadingStartDateText.text = DateFormatClass(requireContext()).computeDateString(currentBook.startDate)

            binding.detailReadingPaperCheckbox.isChecked = currentBook.support?.paperSupport ?: false
            binding.detailReadingEbookCheckbox.isChecked = currentBook.support?.ebookSupport ?: false
            binding.detailReadingAudiobookCheckbox.isChecked = currentBook.support?.audiobookSupport ?: false

            binding.detailReadingPages.text = currentBook.pages.toString()

            detailBook = currentBook
        }
        detailReadingVM.currentBook.observe(viewLifecycleOwner, currentBookObserver)


        val isAccessingDatabaseObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                setViewEnable(false, requireActivity())
                binding.detailReadingLoading.root.visibility = View.VISIBLE
            }

            else {
                setViewEnable(true, requireActivity())
                binding.detailReadingLoading.root.visibility = View.GONE
            }
        }
        detailReadingVM.isAccessingDatabase.observe(viewLifecycleOwner, isAccessingDatabaseObserver)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.detail_reading_book_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //Change icon and Fragment Mode
        return when (item.itemId) {
            R.id.detailReadingMenuEdit -> {
                val navController = findNavController()
                val action = DetailReadingFragmentDirections.actionDetailReadingFragmentToNewReadingBookFragment(detailBook.copy(support = detailBook.support!!.copy()))
                //Anche se effettuo la copia le strutture interne sono passate per riferimento e non vengono copiate, quindi le copio a mano
                //Trovato con bug
                navController.navigate(action)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


}