package it.simone.bookyoulove.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity

data class StartDate(
        @ColumnInfo var startDay: Int,
        @ColumnInfo var startMonth: Int,
        @ColumnInfo var startYear: Int
)


data class EndDate (
    @ColumnInfo var endDay: Int,
    @ColumnInfo var endMonth: Int,
    @ColumnInfo var endYear: Int
)

data class BookSupport (
    @ColumnInfo var paperSupport: Boolean,
    @ColumnInfo var ebookSupport: Boolean,
    @ColumnInfo var audiobookSupport: Boolean
    )


@Entity(primaryKeys = ["title", "author", "readTime"])
data class Book (

    var title: String,
    var author: String,
    var readTime: Int,

    @Embedded var startDate: StartDate?,
    @Embedded var endDate: EndDate?,
    @Embedded var support : BookSupport?,

    var coverName: String,

    var pages: Int?,

    var rate: Float?,

    var labels: String?,

    var readState: Int
    )