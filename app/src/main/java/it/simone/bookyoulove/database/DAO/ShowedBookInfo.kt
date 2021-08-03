package it.simone.bookyoulove.database.DAO

import it.simone.bookyoulove.database.entity.EndDate
import it.simone.bookyoulove.database.entity.StartDate

data class ShowedBookInfo(
    var title : String,
    var author : String,
    var readTime : Int,
    var coverName : String,
    var startDate : StartDate?,
    var endDate : EndDate?,
    var totalRate: Float?
)