package it.simone.bookyoulove.database.entity


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity//(primaryKeys = ["quoteId", "bookId"])
data class Quote (
        @PrimaryKey var quoteId : Long,
        var bookId : Long,

        var quoteText : String,
        var bookTitle : String,
        var bookAuthor : String,

        var favourite : Boolean,
        var toWidget : Boolean,
        var quotePage : Int,
        var quoteChapter : String,
        var quoteThought : String,
        var date : Long
) : Serializable