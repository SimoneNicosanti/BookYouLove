package it.simone.bookyoulove.view

import android.os.Bundle
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
import it.simone.bookyoulove.viewmodel.DetailReadingViewModel
import it.simone.bookyoulove.viewmodel.EndedViewModel


class EndedDetailFragment : Fragment() {

    private lateinit var binding : FragmentEndedDetailBinding
    private val endedDetailVM : DetailReadingViewModel by viewModels()
    private val endedVM : EndedViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentEndedDetailBinding.inflate(inflater, container, false)

        setObservers()
        return binding.root
    }


    private fun setObservers() {

        val currentSelectedObserver = Observer<Book> {
            showSelectedBook(it)
        }
        endedVM.currentSelectedBook.observe(viewLifecycleOwner, currentSelectedObserver)

        val currentShowedObserver = Observer<Book> {
            showSelectedBook(it)
        }
        //endedDetailVM.currentShowedBook.observe(viewLifecycleOwner, currentShowedObserver)
    }

    private fun showSelectedBook(selectedBook: Book?) {

        if (selectedBook != null) {
            if (selectedBook.coverName != "") Picasso.get().load(selectedBook.coverName).into(binding.endedCoverImageView)
            else Picasso.get().load(R.mipmap.book_cover_placeholder).into(binding.endedCoverImageView)

            binding.endedDetailTitle.text = selectedBook.title
            binding.endedDetailAuthor.text = selectedBook.author

        }
    }

}