package it.simone.bookyoulove.model

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.view.ENDED_BOOK_STATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EndingModel(val myAppDatabase: AppDatabase) {

    suspend fun saveTerminatedBook(terminatedBook: Book?) {
        //TODO("Verifica no altri libri stesso titolo e autore in stesso periodo di tempo")
        withContext(Dispatchers.IO) {
            terminatedBook!!.readState = ENDED_BOOK_STATE
            myAppDatabase.bookDao().updateBooks(terminatedBook)
        }
    }

    suspend fun loadEndingBook(endingTitle: String, endingAuthor: String, endingTime: Int): Book {
        //Non ritorno Book? perché se lo sto terminando sicuramente è in DB
        val endingBook : Book
        withContext(Dispatchers.IO) {
            endingBook = myAppDatabase.bookDao().loadSpecificBook(endingTitle, endingAuthor, endingTime)
        }
        return  endingBook
    }
}