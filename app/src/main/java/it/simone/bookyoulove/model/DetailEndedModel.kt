package it.simone.bookyoulove.model

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DetailEndedModel(val myAppDatabase: AppDatabase) {

    suspend fun deleteBook(toDeleteBook: Book) {
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().deleteBooks(toDeleteBook)
            deleteBookQuotes(toDeleteBook)
        }
    }

    private fun deleteBookQuotes(toDeleteBook: Book) {
        myAppDatabase.quoteDao().deleteQuotesByBook(toDeleteBook.keyTitle, toDeleteBook.keyAuthor, toDeleteBook.readTime)
    }

    suspend fun loadEndedDetailBook(endedDetailKeyTitle: String, endedDetailKeyAuthor: String, endedDetailTime: Int): Book {
        val endedBook : Book
        withContext(Dispatchers.IO) {
            endedBook = myAppDatabase.bookDao().loadSpecificBook(endedDetailKeyTitle, endedDetailKeyAuthor, endedDetailTime)
        }
        return endedBook
    }
}