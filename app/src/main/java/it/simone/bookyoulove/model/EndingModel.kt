package it.simone.bookyoulove.model

import it.simone.bookyoulove.Constants.ENDED_BOOK_STATE
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EndingModel(val myAppDatabase: AppDatabase) {

    suspend fun saveTerminatedBook(terminatedBook: Book?) {
        withContext(Dispatchers.IO) {
            terminatedBook!!.readState = ENDED_BOOK_STATE
            myAppDatabase.bookDao().updateBooks(terminatedBook)
        }
    }

    suspend fun loadEndingBook(endingBookId : Long): Book {
        //Non ritorno Book? perché se lo sto terminando sicuramente è in DB
        val endingBook : Book
        withContext(Dispatchers.IO) {
            endingBook = myAppDatabase.bookDao().loadBookById(endingBookId)
        }
        return  endingBook
    }
}