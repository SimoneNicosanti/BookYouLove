package it.simone.bookyoulove.model.ended

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.view.SORT_BY_TITLE
import it.simone.bookyoulove.view.SORT_START_DATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class EndedModel(private val myAppDatabase: AppDatabase) {

    suspend fun loadEndedList(requestedState : Int) : Array<ShowedBookInfo> {
        var loadedList : Array<ShowedBookInfo>
        withContext(Dispatchers.IO) {
            loadedList = myAppDatabase.bookDao().loadShowedBookInfoByState(requestedState)
        }
        return loadedList
    }


    suspend fun sortByDate(bookArray: Array<ShowedBookInfo>, sortType: Int): Array<ShowedBookInfo> {
        val sortedBookArray = arrayListOf<ShowedBookInfo>()
        withContext(Dispatchers.Default) {
            val supportArray = arrayListOf<Pair<String, ShowedBookInfo>>()
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

                supportArray.add(Pair("$formattedYear-$formattedMonth-$formattedDay", book))
            }
            supportArray.sortBy {it.first}

            for (formattedBook in supportArray) {
                sortedBookArray.add(formattedBook.second)
            }
            //I libri più vecchi sono quelli che stanno per primi --> Inverto l'array
            sortedBookArray.reverse()
        }
        return sortedBookArray.toTypedArray()
    }

    suspend fun sortByTitleOrAuthor(notSortedArray: Array<ShowedBookInfo>, sortType: Int): Array<ShowedBookInfo> {
        withContext(Dispatchers.Default) {
            if (sortType == SORT_BY_TITLE) notSortedArray.sortBy{ it.title }
            else notSortedArray.sortBy { it.author }
        }
        return notSortedArray
    }
}
