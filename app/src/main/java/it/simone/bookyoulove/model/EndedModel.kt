package it.simone.bookyoulove.model

import android.util.Log
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.view.SORT_BY_TITLE
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

    suspend fun sortByDate(bookArray : Array<Book>, sortType : Int): Array<Book> {
        var sortedBookArray = arrayOf<Book>()
        withContext(Dispatchers.Default) {
            var supportArray = arrayOf<FormattedBook>()
            for (book in bookArray) {
                var formattedDay: String
                var formattedMonth : String
                var formattedYear : String

                if (sortType == SORT_START_DATE) {
                    formattedDay = if (book.startDate?.startDay!! < 10) "0${book.startDate?.startDay}" else "${book.startDate?.startDay}"
                    formattedMonth = if (book.startDate?.startMonth!! < 10) "0${book.startDate?.startMonth}" else "${book.startDate?.startMonth}"
                    formattedYear = "${book.startDate?.startYear}"
                }
                else {
                    formattedDay = if (book.endDate?.endDay!! < 10) "0${book.endDate?.endDay}" else "${book.endDate?.endDay}"
                    formattedMonth = if (book.endDate?.endMonth!! < 10) "0${book.endDate?.endMonth}" else "${book.endDate?.endMonth}"
                    formattedYear = "${book.endDate?.endYear}"
                }

                supportArray = supportArray.plus(FormattedBook(book, "$formattedYear-$formattedMonth-$formattedDay"))
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

    suspend fun sortByTitleOrAuthor(notSortedArray: Array<Book>, sortType: Int): Array<Book> {
        withContext(Dispatchers.Default) {
            if (sortType == SORT_BY_TITLE) notSortedArray.sortBy{ it.title }
            else notSortedArray.sortBy { it.author }
        }
        return notSortedArray
    }
}

class FormattedBook(val book: Book, var formattedDate: String) {
}