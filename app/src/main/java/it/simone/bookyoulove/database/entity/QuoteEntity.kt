package it.simone.bookyoulove.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(primaryKeys = ["quoteText", "bookTitle", "bookAuthor"])
data class Quote (
    var quoteText : String,
    var bookTitle : String,
    var bookAuthor : String,
    var favourite : Boolean,
    var toWidget : Boolean,
    var quotePage : Int,
    var thought : String,
    @Embedded var date : StartDate
)