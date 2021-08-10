package it.simone.bookyoulove.model

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ModifyQuoteModel(private val myAppDatabase: AppDatabase) {

    suspend fun insertQuoteInDatabase(quoteToAdd: Quote) {
        withContext(Dispatchers.IO) {
            myAppDatabase.quoteDao().insertQuote(quoteToAdd)
        }
    }
}