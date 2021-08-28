package it.simone.bookyoulove.model.ended

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
        myAppDatabase.quoteDao().deleteQuotesByBook(toDeleteBook.bookId)
    }

    suspend fun loadEndedDetailBook(endedDetailBookId: Long): Book {
        val endedBook : Book
        withContext(Dispatchers.IO) {
            endedBook = myAppDatabase.bookDao().loadBookById(endedDetailBookId)
        }
        return endedBook
    }
}