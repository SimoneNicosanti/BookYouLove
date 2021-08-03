package it.simone.bookyoulove.model

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.NotFormattedShowedBookInfo
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.view.READING_BOOK_STATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class NewReadingBookModel(val myAppDatabase : AppDatabase){


    private suspend fun checkPresenceByState(title: String, author: String, readState: Int): Boolean {
        val sameBookArray: Array<NotFormattedShowedBookInfo>
        var isPresent = false
        withContext(Dispatchers.IO) {
            sameBookArray = myAppDatabase.bookDao().loadShowedBookInfoByState(readState)
        }
        //Me li vedo tutti ma non dovrebbero essere molti
        withContext(Dispatchers.Default) {
            for (book in sameBookArray) {
                if (book.title == title && book.author == author) isPresent = true
            }
        }
        return isPresent
        //TODO("Trova un modo per ritornare valori dall'interno di un withContext")
    }


    private suspend fun computeReadTime(title: String, author: String) : Int {
        val sameBookArray: Array<Book>
        var maxReadTime = 0
        withContext(Dispatchers.IO) {
            sameBookArray = myAppDatabase.bookDao().loadSameBook(title, author)
            withContext(Dispatchers.Default) {
                for (book in sameBookArray) {
                    if (book.readTime > maxReadTime) maxReadTime = book.readTime
                }
            }
        }
        return maxReadTime + 1
    }


    private fun capitalizeAll(string: String) : String {
        return string.split(" ").joinToString(" ") { it.toLowerCase(Locale.getDefault()).capitalize(
            Locale.getDefault()
        )
        }
    }


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


    suspend fun loadReadingBookToModifyFromDatabase(title: String, author: String, readingTime: Int): Book {
        val loadedBook : Book
        withContext(Dispatchers.IO) {
            loadedBook = myAppDatabase.bookDao().loadSpecificBook(title, author, readingTime)
        }
        return loadedBook
    }


    suspend fun removeBookFromDatabase(bookToRemove: Book) {
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().deleteBooks(bookToRemove)
        }
    }

    suspend fun addNewBookInDatabase(newReadingBook : Book) {
        if (!checkPresenceByState(newReadingBook.title, newReadingBook.author, READING_BOOK_STATE)) {
            newReadingBook.readTime = computeReadTime(newReadingBook.title, newReadingBook.author)
            withContext(Dispatchers.IO) {
                myAppDatabase.bookDao().insertBooks(newReadingBook)
            }
        }
    }

    suspend fun updateReadingBookInDatabase(bookToUpdate: Book) {
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().updateBooks(bookToUpdate)
        }
    }

}