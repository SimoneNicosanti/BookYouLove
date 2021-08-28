package it.simone.bookyoulove.model.quotes

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteDetailModel(private val myAppDatabase: AppDatabase) {

    suspend fun loadSingleQuotFromDatabase(quoteId : Long, bookId : Long): Quote {
        val loadedQuote : Quote
        withContext(Dispatchers.IO) {
            loadedQuote = myAppDatabase.quoteDao().loadSingleQuote(quoteId, bookId)
        }
        return loadedQuote
    }

    suspend fun deleteCurrentQuote(quoteToDelete: Quote) {
        withContext(Dispatchers.IO) {
            myAppDatabase.quoteDao().deleteQuote(quoteToDelete)
        }
    }
}