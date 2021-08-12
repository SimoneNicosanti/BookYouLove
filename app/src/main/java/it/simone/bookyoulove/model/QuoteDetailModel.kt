package it.simone.bookyoulove.model

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteDetailModel(private val myAppDatabase: AppDatabase) {

    suspend fun loadSingleQuotFromDatabase(detailQuoteText: String, detailQuoteBookTitle: String, detailQuoteBookAuthor: String, detailQuoteReadTime : Int): Quote {
        val loadedQuote : Quote
        withContext(Dispatchers.IO) {
            loadedQuote = myAppDatabase.quoteDao().loadSingleQuote(detailQuoteText, detailQuoteBookTitle, detailQuoteBookAuthor, detailQuoteReadTime)
        }
        return loadedQuote
    }

    suspend fun deleteCurrentQuote(quoteToDelete: Quote) {
        withContext(Dispatchers.IO) {
            myAppDatabase.quoteDao().deleteQuote(quoteToDelete)
        }
    }
}