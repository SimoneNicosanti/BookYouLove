package it.simone.bookyoulove.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import java.io.Serializable

@Entity(primaryKeys = ["quoteId", "bookId"])
data class Quote (
    var quoteId : Long,
    var bookId : Long,

    var quoteText : String,
    var bookTitle : String,
    var bookAuthor : String,

    var favourite : Boolean,
    var toWidget : Boolean,
    var quotePage : Int,
    var quoteChapter : String,
    var quoteThought : String,
    @Embedded var date : StartDate
) : Serializable