package it.simone.bookyoulove.view.quotes

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.DAO.ShowQuoteInfo
import it.simone.bookyoulove.database.entity.Quote
import it.simone.bookyoulove.databinding.FragmentQuoteDetailBinding
import it.simone.bookyoulove.view.dialog.ConfirmDeleteDialogFragment
import it.simone.bookyoulove.viewmodel.QuoteDetailViewModel


class QuoteDetailFragment : Fragment() {

    private lateinit var binding : FragmentQuoteDetailBinding

    private var isFavorite = false
    private lateinit var requestedQuote : Quote

    private val quoteDetailVM : QuoteDetailViewModel by viewModels()

    private val args : QuoteDetailFragmentArgs by navArgs()

    private var quoteDetailFragmentMenu : Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quoteDetailVM.getSingleQuote(args.detailQuoteText, args.detailQuoteBookKeyTitle, args.detailQuoteBookKeyAuthor, args.detailQuoteReadTime)
        setHasOptionsMenu(true)

        childFragmentManager.setFragmentResultListener("deleteKey", this) { _, bundle ->
            if (bundle.getBoolean("deleteConfirm")) {
                quoteDetailVM.deleteCurrentQuote()
                //Notifico la cancellazione per il grafico dei charts
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
            findNavController().previousBackStackEntry?.savedStateHandle?.set<ShowQuoteInfo>("modifiedQuoteInfo",
                    ShowQuoteInfo(
                            quoteText = it.quoteText,
                            keyTitle = it.keyTitle,
                            keyAuthor = it.keyAuthor,
                            readTime = it.readTime,

                            bookTitle = it.bookTitle,
                            bookAuthor = it.bookAuthor,
                            favourite = it.favourite,
                            date = it.date))
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Quote>("modifiedQuoteInfo")
        }

        return binding.root
    }

    private fun setObservers() {

        val currentQuoteObserver = Observer<Quote> { currentQuote ->
            isFavorite = currentQuote.favourite
            binding.quoteDetailTitleTextView.text = currentQuote.bookTitle
            binding.quoteDetailAuthorTextView.text = currentQuote.bookAuthor
            binding.quoteDetailChapterTextView.text = currentQuote.quoteChapter
            binding.quoteDetailPageTextView.text = currentQuote.quotePage.toString()
            binding.quoteDetailQuoteTextTextView.text = currentQuote.quoteText
            binding.quoteDetailYourThoughtTextView.text = currentQuote.quoteThought

            isFavorite = currentQuote.favourite
            /*
            if (quoteDetailFragmentMenu != null) {
                //Il menu è già stato impostato, ma non lo è stato ancora il favorite
                onPrepareOptionsMenu(quoteDetailFragmentMenu!!)
                //Se invece è null sto impostando il favourite PRIMA che venga chiamata la onPrepare e quindi quando sarà chiamata lo imposterà lei
            }*/

            requestedQuote = currentQuote
        }
        quoteDetailVM.currentQuote.observe(viewLifecycleOwner, currentQuoteObserver)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.quote_detail_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        quoteDetailFragmentMenu = menu
        if (isFavorite) menu.findItem(R.id.quoteDetailMenuFavouriteItem).setIcon(R.drawable.ic_round_modify_quote_favorite_on)
        else menu.findItem(R.id.quoteDetailMenuFavouriteItem).setIcon(R.drawable.ic_round_modify_quote_favorite_off)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when(item.itemId) {

            R.id.quoteDetailMenuEditItem -> {
                findNavController().navigate(QuoteListFragmentDirections.actionGlobalModifyQuoteFragment(null, null, 0, requestedQuote.copy()))
                true
            }

            R.id.quoteDetailMenuDeleteItem -> {
                //Non Visualizza Quote nella stringa
                val arguments = bundleOf("itemToDelete" to R.string.quote_delete_string)
                val confirmDeleteDialog = ConfirmDeleteDialogFragment()
                confirmDeleteDialog.arguments = arguments
                confirmDeleteDialog.show(childFragmentManager, "Delete Confirm")
                true
            }

            else ->  super.onOptionsItemSelected(item)
        }

    }
}