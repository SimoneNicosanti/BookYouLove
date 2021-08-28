package it.simone.bookyoulove.model.tbr

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TbrModifyModel(private val myAppDatabase: AppDatabase) {
    
    
    suspend fun loadTbrModifyBookFromDatabase(tbrBookId : Long): Book {
        val loadedBook : Book
        withContext(Dispatchers.IO) {
            loadedBook = myAppDatabase.bookDao().loadBookById(tbrBookId)
        }
        return loadedBook
    }

    suspend fun loadAuthorArrayFromDatabase(): Array<String> {
        var loadedArray: Array<String>
        withContext(Dispatchers.IO) {
            loadedArray = myAppDatabase.bookDao().loadAuthorsList()
            
            withContext(Dispatchers.Default) {loadedArray = loadedArray.distinct().toTypedArray() }
        }
        return loadedArray
    }

    suspend fun saveTbrBookInDatabase(newTbrBook: Book) {
        withContext(Dispatchers.IO) {
            newTbrBook.bookId = computeNewBookId()
            myAppDatabase.bookDao().insertBooks(newTbrBook)
        }
    }

    private suspend fun computeNewBookId(): Long {
        var maxBookId = 0L
        withContext(Dispatchers.IO) {
            val allBookArray = myAppDatabase.bookDao().loadAllBooks()
            withContext(Dispatchers.Default) {
                for (book in allBookArray) {
                    if (book.bookId > maxBookId) maxBookId = book.bookId
                }
            }
        }
        return maxBookId + 1L
    }


    suspend fun updateTbrBookInDatabase(tbrBookToUpdate: Book) {
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().updateBooks(tbrBookToUpdate)
        }
    }
}