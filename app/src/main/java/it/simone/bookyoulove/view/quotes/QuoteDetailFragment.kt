package it.simone.bookyoulove.view.quotes

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.DAO.ShowQuoteInfo
import it.simone.bookyoulove.database.entity.Quote
import it.simone.bookyoulove.databinding.FragmentQuoteDetailBinding
import it.simone.bookyoulove.view.dialog.ConfirmDeleteDialogFragment
import it.simone.bookyoulove.viewmodel.quotes.QuoteDetailViewModel


class QuoteDetailFragment : Fragment() {

    private lateinit var binding : FragmentQuoteDetailBinding

    private lateinit var requestedQuote : Quote

    private val quoteDetailVM : QuoteDetailViewModel by viewModels()

    private val args : QuoteDetailFragmentArgs by navArgs()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quoteDetailVM.getSingleQuote(args.quoteDetailQuoteId, args.quoteDetailBookId)
        setHasOptionsMenu(true)

        childFragmentManager.setFragmentResultListener("deleteKey", this) { _, bundle ->
            if (bundle.getBoolean("deleteConfirm")) {
                quoteDetailVM.deleteCurrentQuote()
                findNavController().previousBackStackEntry?.savedStateHandle?.set("quoteDeletedKey", true)
                findNavController().popBackStack()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentQuoteDetailBinding.inflate(inflater, container, false)

        setObservers()

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Quote>("modifiedQuote")?.observe(viewLifecycleOwner) {
            quoteDetailVM.onQuoteModified(it)
            findNavController().previousBackStackEntry?.savedStateHandle?.set<Quote>("modifiedQuoteInfo", it)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Quote>("modifiedQuoteInfo")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setObservers() {

        val currentQuoteObserver = Observer<Quote> { currentQuote ->

            binding.quoteDetailTitleTextView.text = currentQuote.bookTitle
            binding.quoteDetailAuthorTextView.text = currentQuote.bookAuthor
            binding.quoteDetailChapterTextView.text = currentQuote.quoteChapter
            binding.quoteDetailPageTextView.text = currentQuote.quotePage.toString()
            binding.quoteDetailQuoteTextTextView.text = currentQuote.quoteText
            binding.quoteDetailYourThoughtTextView.text = currentQuote.quoteThought

            if (currentQuote.favourite) binding.quoteDetailFavoriteImage.setImageResource(R.drawable.ic_round_modify_quote_favorite_on)
            else binding.quoteDetailFavoriteImage.setImageResource(R.drawable.ic_round_modify_quote_favorite_off)

            requestedQuote = currentQuote
        }
        quoteDetailVM.currentQuote.observe(viewLifecycleOwner, currentQuoteObserver)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.quote_detail_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when(item.itemId) {

            R.id.quoteDetailMenuEditItem -> {
                findNavController().navigate(QuoteListFragmentDirections.actionGlobalModifyQuoteFragment(requestedQuote.copy(date = requestedQuote.date.copy()), 0L, null, null))
                true
            }

            R.id.quoteDetailMenuDeleteItem -> {
                //Non Visualizza Quote nella stringa
                val arguments = bundleOf("itemToDelete" to getString(R.string.delete_quote_dialog_title))
                val confirmDeleteDialog = ConfirmDeleteDialogFragment()
                confirmDeleteDialog.arguments = arguments
                confirmDeleteDialog.show(childFragmentManager, "Delete Confirm")
                true
            }

            else ->  super.onOptionsItemSelected(item)
        }

    }
}