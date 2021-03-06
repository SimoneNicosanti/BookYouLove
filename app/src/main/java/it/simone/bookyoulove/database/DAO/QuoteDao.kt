package it.simone.bookyoulove.database.DAO

import android.database.Cursor
import androidx.room.*
import it.simone.bookyoulove.database.entity.Quote
import java.io.Serializable


@Dao
interface QuoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQuote(vararg newQuote : Quote)

    @Update
    fun updateQuote(vararg updateQuote: Quote)

    @Delete
    fun deleteQuote(vararg deleteQuote : Quote)

    @Query ("DELETE FROM Quote")
    fun deleteAllQuotes()

    @Query("SELECT * FROM Quote")
    fun loadAllQuotes() : Array<Quote>

    @Query("SELECT quoteId FROM Quote")
    fun loadQuoteKeys() : Array<Long>

    @Query("SELECT * FROM Quote")
    fun loadAllShowQuoteInfo() : Array<ShowQuoteInfo>

    @Query("SELECT * FROM Quote WHERE bookId LIKE :requestedBookId")
    fun loadShowQuoteInfoByBook(requestedBookId: Long) : Array<ShowQuoteInfo>

    @Query("SELECT * FROM Quote WHERE quoteId LIKE :requestedQuoteId")
    fun loadSingleQuote(requestedQuoteId : Long) : Quote

    @Query("SELECT * FROM Quote WHERE bookId LIKE :requestedBookId")
    fun loadQuotesByBook(requestedBookId: Long) : Array<Quote>

    @Query("SELECT * FROM Quote ORDER BY RANDOM() LIMIT 1")
    fun loadRandomQuoteCursor() : Cursor

    @Query("SELECT * FROM Quote ORDER BY RANDOM() LIMIT 1")
    fun loadRandomQuote() : Quote

    @Query("DELETE FROM Quote WHERE bookId LIKE :requestedBookId")
    fun deleteQuotesByBook(requestedBookId: Long)
}

data class ShowQuoteInfo (
        @ColumnInfo(name = "quoteId") var quoteId : Long,
        @ColumnInfo(name = "bookId") var bookId : Long,

        @ColumnInfo(name = "quoteText") var quoteText : String,
        @ColumnInfo(name = "bookTitle") var bookTitle : String,
        @ColumnInfo(name = "bookAuthor") var bookAuthor : String,

        @ColumnInfo(name = "favourite") var favourite : Boolean,
        var date : Long
) : Serializable