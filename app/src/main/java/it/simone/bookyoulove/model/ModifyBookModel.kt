package it.simone.bookyoulove.model

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ModifyBookModel(private val myAppDatabase: AppDatabase) {

    suspend fun loadAuthorArrayFromDatabase() : Array<String> {
        val authorArray : Array<String>
        withContext(Dispatchers.IO) {
            authorArray = myAppDatabase.bookDao().loadAuthorsList()
        }
        val filteredAuthorList : Array<String>
        withContext(Dispatchers.Default) {
            filteredAuthorList = authorArray.distinct().toTypedArray()
        }
        return filteredAuthorList
    }


    suspend fun addNewBookInDatabase(newReadingBook : Book) {
        newReadingBook.bookId = computeNewBookId()
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().insertBooks(newReadingBook)
        }
    }

    private suspend fun computeNewBookId(): Long {
        var maxBookId : Long?
        withContext(Dispatchers.IO) {
            val allBooksArray = myAppDatabase.bookDao().loadBookKeys()
            withContext(Dispatchers.Default) {
                maxBookId = allBooksArray.maxOrNull()
            }
        }

        return (maxBookId ?: 0L) + 1L
    }

    suspend fun updateBookInDatabase(bookToUpdate: Book) {
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().updateBooks(bookToUpdate)
            changeQuotesInfoInDatabase(bookToUpdate)
        }
    }

    private suspend fun changeQuotesInfoInDatabase(changedBook : Book) {

        withContext(Dispatchers.IO) {
            val oldQuotesArray = myAppDatabase.quoteDao().loadQuotesByBook(changedBook.bookId)

            for (quote in oldQuotesArray) {
                quote.bookTitle = changedBook.title
                quote.bookAuthor = changedBook.author

                myAppDatabase.quoteDao().updateQuote(quote)
            }
        }
    }
}