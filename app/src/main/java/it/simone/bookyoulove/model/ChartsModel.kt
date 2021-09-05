package it.simone.bookyoulove.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.BookSupport
import it.simone.bookyoulove.database.entity.Rate
import it.simone.bookyoulove.view.ENDED_BOOK_STATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ChartsBookData(
    @ColumnInfo(name = "bookId") var bookId : Long,
    @ColumnInfo(name = "title") var title : String,
    @ColumnInfo(name = "author") var author : String,
    @ColumnInfo(name = "startDate") var startDate : Long,
    @ColumnInfo(name = "endDate") var endDate : Long,
    @Embedded @ColumnInfo(name = "support") var support : BookSupport,
    @ColumnInfo(name = "pages") var pages : Int,
    @Embedded @ColumnInfo(name = "rate") var rate : Rate
)

class ChartsModel(private val myAppDatabase : AppDatabase) {

    suspend fun getAllChartsDataFromDatabase(): Array<ChartsBookData> {
        val loadedData: Array<ChartsBookData>
        withContext(Dispatchers.IO) {
            loadedData = myAppDatabase.bookDao().loadChartsData(ENDED_BOOK_STATE)
        }
        return sortChartsDataByEndDate(loadedData)
    }

    private suspend fun sortChartsDataByEndDate(notSortedArray : Array<ChartsBookData>): Array<ChartsBookData> {
        withContext(Dispatchers.Default) {
            notSortedArray.sortBy { it.endDate }
        }
        return notSortedArray
    }
}
