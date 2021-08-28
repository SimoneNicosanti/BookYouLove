package it.simone.bookyoulove.model.ended

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ModifyEndedModel(private val myAppDatabase: AppDatabase) {

    suspend fun saveChangedBook(changedBook : Book) {
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().updateBooks(changedBook)
            changeQuotesInfo(changedBook)
        }
    }

    private fun changeQuotesInfo(changedBook : Book) {
        val quotesArray = myAppDatabase.quoteDao().loadQuotesByBook(changedBook.bookId)
        for (quote in quotesArray) {
            quote.bookTitle = changedBook.title
            quote.bookAuthor = changedBook.author
            myAppDatabase.quoteDao().updateQuote(quote)
        }
    }

}