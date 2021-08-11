package it.simone.bookyoulove.database.DAO

import androidx.room.*
import it.simone.bookyoulove.database.entity.Quote
import it.simone.bookyoulove.database.entity.StartDate

data class ShowQuoteInfo (
    @ColumnInfo(name = "quoteText") var quoteText : String,
    @ColumnInfo(name = "keyTitle") var keyTitle : String,
    @ColumnInfo(name = "keyAuthor") var keyAuthor : String,

    @ColumnInfo(name = "bookTitle") var bookTitle : String,
    @ColumnInfo(name = "bookAuthor") var bookAuthor : String,

    @ColumnInfo(name = "favourite") var favourite : Boolean,
    @Embedded var date : StartDate
)

@Dao
interface QuoteDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertQuote(vararg newQuote : Quote)

    @Update
    fun updateQuote(vararg updateQuote: Quote)

    @Delete
    fun deleteQuote(vararg deleteQuote : Quote)

    @Query("SELECT * FROM Quote")
    fun loadAllQuotes() : Array<Quote>

    @Query("SELECT * FROM Quote WHERE keyTitle LIKE :requestedTitle AND keyAuthor LIKE :requestedAuthor")
    fun loadByTitleAndAuthor(requestedTitle: String, requestedAuthor: String) : Array<Quote>

    @Query("SELECT * FROM Quote")
    fun loadAllShowQuoteInfo() : Array<ShowQuoteInfo>

    @Query("SELECT * FROM Quote WHERE keyTitle LIKE :requestedTitle AND keyAuthor LIKE :requestedAuthor AND readTime LIKE :requestedReadTime ")
    fun loadShowQuoteInfoByBook(requestedTitle: String, requestedAuthor: String, requestedReadTime : Int) : Array<ShowQuoteInfo>

    @Query("SELECT * FROM Quote WHERE quoteText LIKE :requestedText AND bookTitle LIKE :requestedTitle AND bookAuthor LIKE :requestedAuthor")
    fun loadSingleQuote(requestedText : String, requestedTitle: String, requestedAuthor: String) : Quote

    @Query("SELECT * FROM Quote WHERE keyTitle LIKE :requestedTitle AND keyAuthor LIKE :requestedAuthor AND readTime LIKE :requestedReadTime")
    fun loadQuotesByBook(requestedTitle: String, requestedAuthor: String, requestedReadTime: Int) : Array<Quote>
}