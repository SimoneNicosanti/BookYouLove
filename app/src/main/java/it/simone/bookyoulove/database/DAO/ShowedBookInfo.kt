package it.simone.bookyoulove.database.DAO

import androidx.room.ColumnInfo
import java.io.Serializable

data class ShowedBookInfo(
    @ColumnInfo(name = "bookId") var bookId : Long,
    @ColumnInfo(name = "title") var title : String,
    @ColumnInfo(name = "author")var author : String,
    @ColumnInfo(name = "coverName") var coverName : String,
    @ColumnInfo(name = "startDate") var startDate : Long?,
    @ColumnInfo(name = "endDate") var endDate : Long?,
    @ColumnInfo(name = "totalRate")  var totalRate: Float?,
    @ColumnInfo(name = "pages") var pages : Int?
) : Serializable