package it.simone.bookyoulove.view.reading

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.databinding.FragmentDetailReadingBinding
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.reading.DetailReadingViewModel
import it.simone.bookyoulove.viewmodel.reading.ReadingViewModel
import java.time.Month
import java.time.format.TextStyle
import java.util.*


class DetailReadingFragment : Fragment() {


    private lateinit var binding : FragmentDetailReadingBinding

    private val detailReadingVM : DetailReadingViewModel by viewModels()
    //private val readingVM : ReadingViewModel by activityViewModels()

    private val args : DetailReadingFragmentArgs by navArgs()

    private lateinit var detailBook : Book


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        detailReadingVM.loadDetailReadingBook(args.detailReadingBookId)
    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        binding = FragmentDetailReadingBinding.inflate(inflater, container, false)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Book>("modifiedBook")?.observe(viewLifecycleOwner) {
            detailReadingVM.onReadingBookModified(it)
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
            if (currentBook.coverName != "") Picasso.get().load(currentBook.coverName).placeholder(R.drawable.book_cover_place_holder).error(
                R.drawable.cover_not_found).into(binding.detailReadingCoverImageView)
            else Picasso.get().load(R.drawable.book_cover_place_holder).into(binding.detailReadingCoverImageView)

            binding.detailReadingTitle.text = currentBook.title
            binding.detailReadingAuthor.text = currentBook.author

            binding.detailReadingStartDateText.text = computeStartDateString(currentBook.startDate)

            binding.detailReadingPaperCheckbox.isChecked = currentBook.support?.paperSupport ?: false
            binding.detailReadingEbookCheckbox.isChecked = currentBook.support?.ebookSupport ?: false
            binding.detailReadingAudiobookCheckbox.isChecked = currentBook.support?.audiobookSupport ?: false

            binding.detailReadingPages.text = currentBook.pages.toString()

            detailBook = currentBook
        }
        detailReadingVM.currentBook.observe(viewLifecycleOwner, currentBookObserver)


        val isAccessingDatabaseObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.detailReadingLoading.root.visibility = View.VISIBLE
            }

            else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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
                val action = DetailReadingFragmentDirections.actionDetailReadingFragmentToNewReadingBookFragment(detailBook.copy(
                        startDate = detailBook.startDate!!.copy(),
                        support = detailBook.support!!.copy()))
                //Anche se effettuo la copia le strutture interne sono passate per riferimento e non vengono copiate, quindi le copio a mano
                //Trovato con bug
                navController.navigate(action)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun computeStartDateString(startDate: StartDate?): CharSequence {
        return "${startDate!!.startDay} ${
            Month.of(startDate.startMonth).getDisplayName(
                TextStyle.FULL, Locale.getDefault()).capitalize(Locale.getDefault())
        } ${startDate.startYear}"
    }


}