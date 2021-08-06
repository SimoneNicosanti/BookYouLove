package it.simone.bookyoulove.database.DAO

import androidx.room.ColumnInfo

data class NotFormattedShowedBookInfo(
    @ColumnInfo(name = "title") var title : String,
    @ColumnInfo(name = "author") var author : String,
    @ColumnInfo(name = "readTime") var readTime : Int,
    @ColumnInfo(name = "coverName") var coverName : String,
    @ColumnInfo(name = "startDay" )var startDay : Int?,
    @ColumnInfo(name = "startMonth") var startMonth : Int?,
    @ColumnInfo(name = "startYear") var startYear : Int?,
    @ColumnInfo(name = "endDay") var endDay : Int?,
    @ColumnInfo(name = "endMonth") var endMonth : Int?,
    @ColumnInfo(name = "endYear") var endYear : Int?,
    @ColumnInfo(name = "totalRate") var totalRate : Float?
)