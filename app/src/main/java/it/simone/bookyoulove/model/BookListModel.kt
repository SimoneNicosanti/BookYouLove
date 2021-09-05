package it.simone.bookyoulove.model

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.view.TBR_BOOK_STATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookListModel(private val myAppDatabase: AppDatabase) {


    suspend fun loadRequestedShownBookInfo(requestedState: Int): MutableList<ShowedBookInfo> {
        val loadedArray : Array<ShowedBookInfo>
        withContext(Dispatchers.IO) {
            loadedArray = myAppDatabase.bookDao().loadShowedBookInfoByState(requestedState)
        }
        return loadedArray.toMutableList()
    }

    suspend fun deleteBook(bookId : Long) {
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().deleteBookById(bookId)
            myAppDatabase.quoteDao().deleteQuotesByBook(bookId)
        }
    }

    suspend fun moveReadingBookToTbr(bookId : Long) {
        withContext(Dispatchers.IO) {
            val book = myAppDatabase.bookDao().loadBookById(bookId)
            book.readState = TBR_BOOK_STATE
            myAppDatabase.bookDao().updateBooks(book)
        }
    }
}