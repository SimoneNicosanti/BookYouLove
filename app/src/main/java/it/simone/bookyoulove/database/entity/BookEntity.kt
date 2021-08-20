package it.simone.bookyoulove.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// Per poter rendere Serializable Book devo rendere tali anche tutte le classi interne

data class StartDate (
        @ColumnInfo var startDay: Int,
        @ColumnInfo var startMonth: Int,
        @ColumnInfo var startYear: Int
) : Serializable


data class EndDate (
    @ColumnInfo var endDay: Int,
    @ColumnInfo var endMonth: Int,
    @ColumnInfo var endYear: Int
) : Serializable


data class Rate (
    var totalRate : Float,
    var styleRate : Float,
    var emotionRate : Float,
    var plotRate : Float,
    var characterRate : Float
    ) : Serializable


data class BookSupport (
    @ColumnInfo var paperSupport: Boolean,
    @ColumnInfo var ebookSupport: Boolean,
    @ColumnInfo var audiobookSupport: Boolean
    ) : Serializable


@Entity
data class Book (
    @PrimaryKey var bookId : Long,

    var title : String,
    var author : String,

    @Embedded var startDate: StartDate?,
    @Embedded var endDate: EndDate?,
    @Embedded var support : BookSupport?,

    var coverName: String,

    var pages: Int?,

    @Embedded var rate: Rate?,

    //var labels: String?,

    var finalThought : String,

    var readState: Int
    ) : Serializable