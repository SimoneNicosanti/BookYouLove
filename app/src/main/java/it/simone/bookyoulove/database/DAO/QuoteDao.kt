package it.simone.bookyoulove.database.DAO

import androidx.room.*
import it.simone.bookyoulove.database.entity.Quote

@Dao
interface QuoteDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertQuote(vararg newQuote : Quote)

    @Update
    fun updateQuote(vararg updateQuote: Quote)

    @Delete
    fun deleteQuote(vararg deleteQuote : Quote)

    @Query("SELECT * FROM Quote")
    fun loadAllQuotes() : ArrayList<Quote>

    @Query("SELECT * FROM Quote WHERE bookTitle LIKE :requestedTitle AND bookAuthor LIKE :requestedAuthor")
    fun loadByTitleAndAuthor(requestedTitle: String, requestedAuthor: String)
}