package it.simone.bookyoulove.model


import it.simone.bookyoulove.database.DAO.ShowQuoteInfo
import it.simone.bookyoulove.database.entity.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class QuoteList(private var loadedArray : MutableList<ShowQuoteInfo>) {

    private var currentSelectedQuote: ShowQuoteInfo? = null

    operator fun get(position : Int): ShowQuoteInfo {
        return this.loadedArray[position]
    }


    fun getValue(): MutableList<ShowQuoteInfo> {
        return this.loadedArray
    }

    fun setSelectedQuote(selectedQuote : ShowQuoteInfo) {
        currentSelectedQuote = selectedQuote
    }

    private fun findOriginalPosition(): Int {
        return this.loadedArray.indexOf(currentSelectedQuote)
    }


    suspend fun onModifiedQuote(modifiedQuote: Quote) {
        withContext(Dispatchers.Default) {
            if (currentSelectedQuote != null) {
                val modifiedQuoteInfo = ShowQuoteInfo(
                        quoteId = modifiedQuote.quoteId,
                        bookId = modifiedQuote.bookId,
                        quoteText = modifiedQuote.quoteText,

                        bookTitle = modifiedQuote.bookTitle,
                        bookAuthor = modifiedQuote.bookAuthor,
                        favourite = modifiedQuote.favourite,
                        date = modifiedQuote.date)
                val originalPosition = findOriginalPosition()
                this@QuoteList.loadedArray[originalPosition] = modifiedQuoteInfo
                currentSelectedQuote = null
            }
        }
    }


    suspend fun onQuoteDeleted() {
        withContext(Dispatchers.Default) {
            if (currentSelectedQuote != null) {
                val originalPosition = findOriginalPosition()
                this@QuoteList.loadedArray.removeAt(originalPosition)
                //searchByContents(searchField)
                currentSelectedQuote = null
            }
        }
    }

}