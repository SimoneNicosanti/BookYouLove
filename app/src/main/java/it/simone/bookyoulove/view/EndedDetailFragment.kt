package it.simone.bookyoulove.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.databinding.FragmentEndedDetailBinding
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.DetailEndedViewModel
import it.simone.bookyoulove.viewmodel.DetailReadingViewModel
import it.simone.bookyoulove.viewmodel.EndedViewModel


class EndedDetailFragment : Fragment() {

    private lateinit var binding : FragmentEndedDetailBinding
    private val endedDetailVM : DetailEndedViewModel by viewModels()
    private val endedVM : EndedViewModel by activityViewModels()

    private var loadingDialog = LoadingDialogFragment()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentEndedDetailBinding.inflate(inflater, container, false)

        setObservers()

        return binding.root
    }


    private fun setObservers() {

        val isAccessingDatabaseObserver = Observer<Boolean> {
            if (it) {
                loadingDialog.showNow(childFragmentManager, "Loading Dialog")
            }
            else {
                if (loadingDialog.isAdded) {
                    loadingDialog.dismiss()
                    loadingDialog = LoadingDialogFragment()
                }
            }
        }
        endedDetailVM.isAccessingDatabase.observe(viewLifecycleOwner, isAccessingDatabaseObserver)

        val requestedBookObserver = Observer<Book?> { requestedBook ->
            if (!endedDetailVM.loadedOnce) {
                endedDetailVM.loadedOnce = true
                endedDetailVM.showedBook = requestedBook
                endedDetailVM.setShowedBook()
            }
        }
        endedVM.currentSelectedBook.observe(viewLifecycleOwner, requestedBookObserver)

        val currentBookObserver = Observer<Book>  { currentBook ->
            binding.endedDetailTitle.text = currentBook.title
            binding.endedDetailAuthor.text = currentBook.author

            if (currentBook.coverName != "") Picasso.get().load(currentBook.coverName).placeholder(R.drawable.book_cover_place_holder).error(R.drawable.cover_not_found).into(binding.endedDetailCoverImageView)
            else Picasso.get().load(R.drawable.book_cover_place_holder).into(binding.endedDetailCoverImageView)

            binding.endedDetailPagesTextView.text = currentBook.pages.toString()
            binding.endedDetailRatingBar.rating = currentBook.rate!!

            binding.endedDetailPaperCheckBox.isChecked = currentBook.support?.paperSupport ?: false
            binding.endedDetailEbookCheckBox.isChecked = currentBook.support?.ebookSupport ?: false
            binding.endedDetailAudiobookCheckBox.isChecked = currentBook.support?.audiobookSupport ?: false
        }
        endedDetailVM.currentBook.observe(viewLifecycleOwner, currentBookObserver)


    }

    private fun setUserInterface(active: Boolean) {
        binding.endedDetailCoverImageView.isClickable = !active


    }
}