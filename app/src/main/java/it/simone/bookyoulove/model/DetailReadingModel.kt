package it.simone.bookyoulove.model

import android.content.Context
import android.util.Log
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DetailReadingModel(private val myAppDatabase: AppDatabase) {


    suspend fun getBookFromDatabase(title: String, author: String, readTime: Int): Book {
        val requestedBook: Book
        withContext(Dispatchers.IO) {
            requestedBook = myAppDatabase.bookDao().loadSpecificBook(title, author, readTime)
        }
        return requestedBook
    }

    suspend fun removeBookFromDatabase(toDeleteBook : Book) {
        Log.i("Nicosanti", "Deleting")
        withContext(Dispatchers.IO) {
            val deleteIndex = myAppDatabase.bookDao().deleteBooks(toDeleteBook)
            Log.i("Nicosanti", "Deleted $deleteIndex")
        }
    }

    suspend fun addBookInDatabase(toAddBook: Book) {
        val sameBookArray : Array<Book>
        withContext(Dispatchers.IO) {
            sameBookArray = myAppDatabase.bookDao().loadSameBook(toAddBook.title, toAddBook.author)

            val presented : Boolean = checkPresence(sameBookArray, toAddBook.readState)

            if (!presented) {
                val newReadTime = sameBookArray.size + 1
                toAddBook.readTime = newReadTime

                myAppDatabase.bookDao().insertBooks(toAddBook)
            }
        }
    }


    suspend fun updateBookInDatabase(toUpdateBook: Book) {
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().updateBooks(toUpdateBook)
            Log.i("Nicosanti", "Model Updating")
        }
    }


    private fun checkPresence(sameBookArray: Array<Book>, readState: Int): Boolean {
        for (book in sameBookArray) {
            if (book.readState == readState) return true
        }
        return false
    }
}

