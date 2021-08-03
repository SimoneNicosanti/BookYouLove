package it.simone.bookyoulove.model

import android.content.Context
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.NotFormattedShowedBookInfo
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.database.entity.EndDate
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.view.READING_BOOK_STATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReadingModel(context: Context) {

    private val myAppDatabase : AppDatabase = AppDatabase.getDatabaseInstance(context)

    suspend fun getReadingBookArray(): Array<ShowedBookInfo> {
        val array: Array<ShowedBookInfo>
        withContext(Dispatchers.IO) {
            array = formatLoadedBookInfo(myAppDatabase.bookDao().loadShowedBookInfoByState(READING_BOOK_STATE))
        }
        return array
    }

    private fun formatLoadedBookInfo(loadedArray: Array<NotFormattedShowedBookInfo>): Array<ShowedBookInfo> {
        val supportList : MutableList<ShowedBookInfo> = mutableListOf()
        for (elem in loadedArray) {

            val startDate = if (elem.startDay != null) StartDate(elem.startDay!!, elem.startMonth!!, elem.startYear!!) else null
            val endDate = if (elem.endDay != null) EndDate(elem.endDay!!, elem.endMonth!!, elem.endYear!!) else null

            val newElem = ShowedBookInfo(elem.title, elem.author, elem.readTime, elem.coverName, startDate, endDate, elem.totalRate)

            supportList.add(newElem)
        }
        return supportList.toTypedArray()
    }

}