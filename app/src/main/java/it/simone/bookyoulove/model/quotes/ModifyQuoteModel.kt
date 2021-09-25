package it.simone.bookyoulove.model.quotes

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ModifyQuoteModel(private val myAppDatabase: AppDatabase) {

    suspend fun insertQuoteInDatabase(quoteToAdd: Quote) {
        quoteToAdd.quoteId = computeNewQuoteId()
        withContext(Dispatchers.IO) {
            myAppDatabase.quoteDao().insertQuote(quoteToAdd)
        }
    }

    private suspend fun computeNewQuoteId(): Long {
        val maxQuoteId : Long?

        withContext(Dispatchers.IO) {
            val quotesKeysArray : Array<Long> = myAppDatabase.quoteDao().loadQuoteKeys()
            withContext(Dispatchers.Default) {
               maxQuoteId = quotesKeysArray.maxOrNull()
            }
        }

        return (maxQuoteId ?: 0L) + 1L
    }


    suspend fun updateQuoteInDatabase(quoteToUpdate: Quote) {
        withContext(Dispatchers.IO) {
            myAppDatabase.quoteDao().updateQuote(quoteToUpdate)
        }
    }

}