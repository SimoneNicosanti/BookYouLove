package it.simone.bookyoulove.database.DAO

data class NotFormattedShowedBookInfo(
    var title : String,
    var author : String,
    var readTime : Int,
    var coverName : String,
    var startDay : Int?,
    var startMonth : Int?,
    var startYear : Int?,
    var endDay : Int?,
    var endMonth : Int?,
    var endYear : Int?,
    var totalRate : Float?
)