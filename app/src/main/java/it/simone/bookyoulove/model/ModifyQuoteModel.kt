package it.simone.bookyoulove.model

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ModifyQuoteModel(private val myAppDatabase: AppDatabase) {

    suspend fun insertQuoteInDatabase(quoteToAdd: Quote) {
        quoteToAdd.quoteId = computeNewQuoteId(quoteToAdd.bookId)
        withContext(Dispatchers.IO) {
            myAppDatabase.quoteDao().insertQuote(quoteToAdd)
        }
    }

    private suspend fun computeNewQuoteId(bookId: Long): Long {
        var maxQuoteId = 0L

        withContext(Dispatchers.IO) {
            val bookQuotesArray : Array<Quote> = myAppDatabase.quoteDao().loadQuotesByBook(bookId)
            withContext(Dispatchers.Default) {
                for (quote in bookQuotesArray) {
                    if (quote.quoteId > maxQuoteId) maxQuoteId = quote.quoteId
                }
            }
        }

        return maxQuoteId + 1
    }


    suspend fun updateQuoteInDatabase(quoteToUpdate: Quote) {
        withContext(Dispatchers.IO) {
            myAppDatabase.quoteDao().updateQuote(quoteToUpdate)
        }
    }

}