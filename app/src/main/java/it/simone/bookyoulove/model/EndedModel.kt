package it.simone.bookyoulove.model

import android.util.Log
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.view.SORT_START_DATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class EndedModel(private val myAppDatabase: AppDatabase) {

    suspend fun loadReadList(requestedState : Int) : Array<Book> {
        val loadedList : Array<Book>
        withContext(Dispatchers.IO) {
            loadedList = myAppDatabase.bookDao().loadBookArrayByState(requestedState)
        }
        return loadedList
    }

    suspend fun sortByStartDate(bookArray : Array<Book>): Array<Book> {
        var sortedBookArray = arrayOf<Book>()
        withContext(Dispatchers.Default) {
            var supportArray = arrayOf<FormattedBook>()
            for (book in bookArray) {
                supportArray = supportArray.plus(FormattedBook(book, "${book.startDate?.startYear}-${book.startDate?.startMonth}-${book.startDate?.startDay}"))

            }
            supportArray.sortBy {it.formattedDate}

            for (formattedBook in supportArray) {
                sortedBookArray = sortedBookArray.plus(formattedBook.book)
                Log.i("Nicosanti", "Libro ${formattedBook.book.title}")
            }
            //I libri più vecchi sono quelli che stanno per primi --> Inverto l'array
            sortedBookArray.reverse()
            Log.i("Nicosanti", "sorting")
        }
        return sortedBookArray
    }
}

class FormattedBook(val book: Book, var formattedDate: String) {
}