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
        val returnArray = arrayListOf<ShowQuoteInfo>()
        withContext(Dispatchers.Default) {
            val supportArray = arrayListOf<Pair<String, ShowQuoteInfo>>()

            for (info in quotesArray) {
                val formattedDay = if (info.date.startDay < 10) "0${info.date.startDay}" else "${info.date.startDay}"
                val formattedMonth = if (info.date.startMonth < 10) "0${info.date.startMonth}" else "${info.date.startMonth}"
                val formattedYear = "${info.date.startYear}"

                supportArray.add(Pair("${formattedYear}-$formattedMonth-$formattedDay" , info))
            }

            supportArray.sortBy { it.first }
            supportArray.reverse()

            for (pair in supportArray) {
                returnArray.add(pair.second)
            }
        }

        return returnArray.toTypedArray()
    }
}