package it.simone.bookyoulove.view.quotes

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.navArgs
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Quote
import it.simone.bookyoulove.databinding.FragmentQuoteDetailBinding
import it.simone.bookyoulove.viewmodel.QuoteDetailViewModel


class QuoteDetailFragment : Fragment() {

    private lateinit var binding : FragmentQuoteDetailBinding

    private var isFavorite = false

    private val quoteDetailVM : QuoteDetailViewModel by viewModels()

    private val args : QuoteDetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quoteDetailVM.getSingleQuote(args.detailQuoteText, args.detailQuoteBookKeyTitle, args.detailQuoteBookKeyAuthor)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentQuoteDetailBinding.inflate(inflater, container, false)

        setObservers()
        return binding.root
    }

    private fun setObservers() {

        val currentQuoteObserver = Observer<Quote> { currentQuote ->

            isFavorite = currentQuote.favourite
        }
        quoteDetailVM.currentQuote.observe(viewLifecycleOwner, currentQuoteObserver)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.quote_detail_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (isFavorite) menu.findItem(R.id.quoteDetailMenuFavouriteItem).setIcon(R.drawable.ic_round_modify_quote_favorite_on)
        else menu.findItem(R.id.quoteDetailMenuFavouriteItem).setIcon(R.drawable.ic_round_modify_quote_favorite_off)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when(item.itemId) {

            else ->  super.onOptionsItemSelected(item)
        }

    }
}