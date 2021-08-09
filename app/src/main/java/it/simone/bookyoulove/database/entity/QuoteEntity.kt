package it.simone.bookyoulove.database.entity

import androidx.room.Embedded
import androidx.room.Entity

@Entity(primaryKeys = ["quoteText", "bookTitle", "bookAuthor"])
data class Quote (
    var quoteText : String,
    var bookTitle : String,
    var bookAuthor : String,
    var favourite : Boolean,
    var toWidget : Boolean,
    var quotePage : Int,
    var quoteChapter : String,
    var quoteThought : String,
    @Embedded var date : StartDate
)