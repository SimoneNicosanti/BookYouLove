package it.simone.bookyoulove.database.entity

import androidx.room.Embedded
import androidx.room.Entity

@Entity(primaryKeys = ["quoteText", "keyTitle", "keyAuthor", "readTime"])
data class Quote (
    var quoteText : String,
    var keyTitle : String,
    var keyAuthor : String,
    var readTime : Int,

    var bookTitle : String,
    var bookAuthor : String,

    var favourite : Boolean,
    var toWidget : Boolean,
    var quotePage : Int,
    var quoteChapter : String,
    var quoteThought : String,
    @Embedded var date : StartDate
)