package it.simone.bookyoulove.model.quotes

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowQuoteInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteListModel(private val myAppDatabase : AppDatabase) {

    suspend fun loadAllQuotesFromDatabase(): Array<ShowQuoteInfo> {
        var quotesArray : Array<ShowQuoteInfo>
        withContext(Dispatchers.IO) {
            quotesArray = myAppDatabase.quoteDao().loadAllShowQuoteInfo()
            quotesArray = sortShowInfoByDate(quotesArray)
        }
        return quotesArray
    }

    suspend fun loadQuotesByBookFromDatabase(bookId: Long): Array<ShowQuoteInfo> {
        var quotesArray : Array<ShowQuoteInfo>
        withContext(Dispatchers.IO) {
            quotesArray = myAppDatabase.quoteDao().loadShowQuoteInfoByBook(bookId)
            quotesArray = sortShowInfoByDate(quotesArray)
        }
        return quotesArray
    }

    private suspend fun sortShowInfoByDate(quotesArray: Array<ShowQuoteInfo>): Array<ShowQuoteInfo> {
        withContext(Dispatchers.Default) {
            quotesArray.sortBy { it.date }
            quotesArray.reverse()
        }

        return quotesArray
    }
}