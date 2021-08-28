package it.simone.bookyoulove.model.reading

import android.content.Context
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.view.READING_BOOK_STATE
import it.simone.bookyoulove.view.TBR_BOOK_STATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReadingModel(context: Context) {

    private val myAppDatabase : AppDatabase = AppDatabase.getDatabaseInstance(context)

    suspend fun getReadingBookArray(): Array<ShowedBookInfo> {
        val array: Array<ShowedBookInfo>
        withContext(Dispatchers.IO) {
            array = myAppDatabase.bookDao().loadShowedBookInfoByState(READING_BOOK_STATE)
        }
        return array
    }

    suspend fun moveBookToTbrInDatabase(bookId: Long) {
        withContext(Dispatchers.IO) {
            val movingBook = myAppDatabase.bookDao().loadBookById(bookId)
            movingBook.readState = TBR_BOOK_STATE
            movingBook.startDate = null
            movingBook.support = null
            myAppDatabase.bookDao().updateBooks(movingBook)
        }
    }

    suspend fun deleteReadingBookFromDatabase(bookId: Long) {
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().deleteBookById(bookId)
            myAppDatabase.quoteDao().deleteQuotesByBook(bookId)
        }
    }

}