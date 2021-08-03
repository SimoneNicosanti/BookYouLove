package it.simone.bookyoulove.view

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.CheckBox
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.databinding.FragmentDetailReadingBinding
import it.simone.bookyoulove.view.dialog.CoverLinkPickerFragment
import it.simone.bookyoulove.view.dialog.DatePickerFragment
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.view.dialog.PagesPickerFragment
import it.simone.bookyoulove.viewmodel.DetailReadingViewModel
import it.simone.bookyoulove.viewmodel.ReadingViewModel
import java.time.Month
import java.time.format.TextStyle
import java.util.*


class DetailReadingFragment : Fragment() {


    private lateinit var binding : FragmentDetailReadingBinding

    private val detailReadingVM : DetailReadingViewModel by viewModels()

    private val args : DetailReadingFragmentArgs by navArgs()

    private var loadingDialog = LoadingDialogFragment()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        detailReadingVM.loadDetailReadingBook(args.detailTitle, args.detailAuthor, args.detailTime)
    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        binding = FragmentDetailReadingBinding.inflate(inflater, container, false)

        setObservers()
        return binding.root
    }

    private fun setObservers() {

        val currentBookObserver = Observer<Book> { currentBook ->

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
        }
        detailReadingVM.currentBook.observe(viewLifecycleOwner, currentBookObserver)


        val isAccessingDatabaseObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                loadingDialog.showNow(childFragmentManager, "Loading Dialog")
            }
            else {
                if (loadingDialog.isAdded) {
                    loadingDialog.dismiss()
                    loadingDialog = LoadingDialogFragment()
                }
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
                val action = DetailReadingFragmentDirections.actionDetailReadingFragmentToNewReadingBookFragment(args.detailTitle, args.detailAuthor, args.detailTime)
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