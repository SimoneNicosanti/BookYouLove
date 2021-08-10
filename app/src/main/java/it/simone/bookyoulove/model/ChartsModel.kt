package it.simone.bookyoulove.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.NotFormattedChartsBookData
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.database.entity.BookSupport
import it.simone.bookyoulove.database.entity.EndDate
import it.simone.bookyoulove.database.entity.Rate
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.view.ENDED_BOOK_STATE
import it.simone.bookyoulove.view.SORT_START_DATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.ArrayList

data class ChartsBookData(
    @ColumnInfo(name = "title") var title : String,
    @ColumnInfo(name = "author") var author : String,
    @ColumnInfo(name = "readTime") var readTime : Int,
    @Embedded @ColumnInfo(name = "startDate") var startDate : StartDate,
    @Embedded @ColumnInfo(name = "endDate") var endDate : EndDate,
    @Embedded @ColumnInfo(name = "support") var support : BookSupport,
    @ColumnInfo(name = "pages") var pages : Int,
    @Embedded @ColumnInfo(name = "rate") var rate : Rate
)

class ChartsModel(private val myAppDatabase : AppDatabase) {

    suspend fun getAllChartsDataFromDatabase(): Array<ChartsBookData> {
        val loadedData: Array<ChartsBookData>
        val formattedData: ArrayList<ChartsBookData>
        withContext(Dispatchers.IO) {
            loadedData = myAppDatabase.bookDao().loadChartsData(ENDED_BOOK_STATE)
        }
        return sortChartsDataByEndDate(loadedData)
    }

    private suspend fun sortChartsDataByEndDate(notSortedArray : Array<ChartsBookData>): Array<ChartsBookData> {
        val sortedArray = arrayListOf<ChartsBookData>()
        val supportArray = arrayListOf<ChartsBookDataStringDate>()
        withContext(Dispatchers.Default) {
            for (data in notSortedArray) {

                val formattedDay: String = if (data.endDate.endDay < 10) "0${data.endDate.endDay}" else "${data.endDate.endDay}"
                val formattedMonth : String = if (data.endDate.endMonth < 10) "0${data.endDate.endMonth}" else "${data.endDate.endMonth}"
                val formattedYear : String = "${data.endDate.endYear}"

                supportArray.add(ChartsBookDataStringDate(data, "$formattedYear-$formattedMonth-$formattedDay"))
            }

            supportArray.sortBy { it.formattedDate }

            for (formattedData in supportArray) {
                sortedArray.add(formattedData.chartsBookData)
            }
        }
        return sortedArray.toTypedArray()
    }
}

class ChartsBookDataStringDate( val chartsBookData: ChartsBookData, val formattedDate : String)