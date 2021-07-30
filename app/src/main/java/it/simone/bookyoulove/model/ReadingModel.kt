package it.simone.bookyoulove.model

import android.content.Context
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.view.READING_BOOK_STATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ReadingModel(context: Context) {

    private val myAppDatabase : AppDatabase = AppDatabase.getDatabaseInstance(context)

    suspend fun getReadingBookArray(): Array<Book> {
        val array: Array<Book>
        withContext(Dispatchers.IO) {
            array = myAppDatabase.bookDao().loadBookArrayByState(READING_BOOK_STATE)
        }
        return array
    }

    suspend fun loadTermBook(bookInfo: Book) : Book{
        val terminatedBook : Book
        withContext(Dispatchers.IO) {
            terminatedBook = myAppDatabase.bookDao().loadSpecificBook(bookInfo.title, bookInfo.author, bookInfo.readTime)
        }
        return terminatedBook
    }

    suspend fun saveTerminatedBook(termBook: Book) {
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().updateBooks(termBook)
        }
    }
}