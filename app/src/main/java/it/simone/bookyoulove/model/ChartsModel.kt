package it.simone.bookyoulove.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.NotFormattedChartsBookData
import it.simone.bookyoulove.database.entity.BookSupport
import it.simone.bookyoulove.database.entity.EndDate
import it.simone.bookyoulove.database.entity.Rate
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.view.ENDED_BOOK_STATE
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
        val loadedData : Array<ChartsBookData>
        val formattedData : ArrayList<ChartsBookData>
        withContext(Dispatchers.IO) {
            loadedData = myAppDatabase.bookDao().loadChartsData(ENDED_BOOK_STATE)
        }
        return loadedData
    }


}