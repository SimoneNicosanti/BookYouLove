package it.simone.bookyoulove.model

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowQuoteInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteListModel(private val myAppDatabase : AppDatabase) {

    suspend fun loadAllQuotesFromDatabase(): Array<ShowQuoteInfo> {
        var quotesArray : Array<ShowQuoteInfo>
        withContext(Dispatchers.IO) {
            quotesArray = myAppDatabase.quoteDao().loadAllShowQuoteInfo()
            quotesArray = orderShowInfoByDate(quotesArray)
        }
        return quotesArray
    }

    suspend fun loadQuotesByBookFromDatabase(bookKeyTitle: String, bookKeyAuthor: String, bookReadTime: Int): Array<ShowQuoteInfo> {
        var quotesArray : Array<ShowQuoteInfo>
        withContext(Dispatchers.IO) {
            quotesArray = myAppDatabase.quoteDao().loadShowQuoteInfoByBook(bookKeyTitle, bookKeyAuthor, bookReadTime)
            quotesArray = orderShowInfoByDate(quotesArray)
        }
        return quotesArray
    }

    private suspend fun orderShowInfoByDate(quotesArray: Array<ShowQuoteInfo>): Array<ShowQuoteInfo> {
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