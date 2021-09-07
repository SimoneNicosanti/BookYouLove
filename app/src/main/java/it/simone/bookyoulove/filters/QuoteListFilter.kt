package it.simone.bookyoulove.filters

import android.widget.Filter
import it.simone.bookyoulove.adapter.QuoteListAdapter
import it.simone.bookyoulove.database.DAO.ShowQuoteInfo
import java.util.*

class QuoteListFilter(private val quoteSetAll: MutableList<ShowQuoteInfo>,
                      private val quoteSet: MutableList<ShowQuoteInfo>,
                      private val adapter: QuoteListAdapter,
                      private val favoriteSearch : Boolean): Filter() {


    override fun performFiltering(queryField: CharSequence?): FilterResults {
        val filteredList: MutableList<ShowQuoteInfo>
        if (!favoriteSearch) {
                filteredList = if (queryField == null || queryField == "") quoteSetAll
                else quoteSetAll.filter {
                    it.quoteText.toLowerCase(Locale.ROOT)
                        .contains(queryField.toString().toLowerCase(Locale.getDefault()))
                } as MutableList<ShowQuoteInfo>
        }
        else {
            filteredList = quoteSetAll.filter {it.favourite} as MutableList<ShowQuoteInfo>
        }

        val filterResult = FilterResults()
        filterResult.values = filteredList
        return filterResult
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        quoteSet.clear()
        quoteSet.addAll(results.values as Collection<ShowQuoteInfo>)
        adapter.notifyDataSetChanged()
    }
}