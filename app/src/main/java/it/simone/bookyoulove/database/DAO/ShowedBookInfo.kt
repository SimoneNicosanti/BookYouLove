package it.simone.bookyoulove.database.DAO

import androidx.room.ColumnInfo
import androidx.room.Embedded
import it.simone.bookyoulove.database.entity.EndDate
import it.simone.bookyoulove.database.entity.StartDate
import java.io.Serializable

data class ShowedBookInfo(
    @ColumnInfo(name = "bookId") var bookId : Long,
    @ColumnInfo(name = "title") var title : String,
    @ColumnInfo(name = "author")var author : String,
    @ColumnInfo(name = "coverName") var coverName : String,
    @Embedded @ColumnInfo(name = "startDate") var startDate : StartDate?,
    @Embedded @ColumnInfo(name = "endDate") var endDate : EndDate?,
    @ColumnInfo(name = "totalRate")  var totalRate: Float?,
    @ColumnInfo(name = "pages") var pages : Int?
) : Serializable