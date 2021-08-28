package it.simone.bookyoulove.viewmodel.quotes

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowQuoteInfo
import it.simone.bookyoulove.database.entity.Quote
import it.simone.bookyoulove.model.quotes.QuoteListModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuoteListViewModel(application : Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val quoteListModel = QuoteListModel(myAppDatabase)

    val currentQuotesArray = MutableLiveData<Array<ShowQuoteInfo>>(arrayOf())
    val isAccessingDatabase = MutableLiveData(false)

    private var currentPosition : Int = -1

    private var originalArray : Array<ShowQuoteInfo> = arrayOf()

    private var searchField : String? = null

    private var loadedOnce = false

    fun getAllQuotes() {
        if (!loadedOnce) {
            isAccessingDatabase.value = true
            viewModelScope.launch {
                originalArray = quoteListModel.loadAllQuotesFromDatabase()
                loadedOnce = true
                currentQuotesArray.value = originalArray
                isAccessingDatabase.value = false
            }
        }
    }

    fun getQuotesByBookId(bookId : Long) {
        if (!loadedOnce) {
            isAccessingDatabase.value = true
            viewModelScope.launch {
                currentQuotesArray.value = quoteListModel.loadQuotesByBookFromDatabase(bookId)
                loadedOnce = true
                originalArray = currentQuotesArray.value!!
                isAccessingDatabase.value = false
            }
        }
    }

    fun onQuoteDeleted() {
        viewModelScope.launch {
            val originalPosition = findOriginalPosition()
            withContext(Dispatchers.Default) {
                val supportArrayList = originalArray.toMutableList()
                supportArrayList.removeAt(originalPosition)
                originalArray = supportArrayList.toTypedArray()
            }
            currentQuotesArray.value = originalArray
            //searchByContents(searchField)
        }
    }

    fun setCurrentPosition(position: Int) {
        currentPosition = position
    }

    fun onModifiedQuote(modifiedQuote: Quote) {
        val modifiedQuoteInfo = ShowQuoteInfo(
                quoteId = modifiedQuote.quoteId,
                bookId = modifiedQuote.bookId,
                quoteText = modifiedQuote.quoteText,

                bookTitle = modifiedQuote.bookTitle,
                bookAuthor = modifiedQuote.bookAuthor,
                favourite = modifiedQuote.favourite,
                date = modifiedQuote.date)
        viewModelScope.launch {
            val originalPosition = findOriginalPosition()
            originalArray[originalPosition] = modifiedQuoteInfo
            currentQuotesArray.value = originalArray
            currentPosition = -1
            //searchByContents(searchField)
        }
    }

    fun searchByContents(newText: String?) {
        searchField = newText ?: ""
        Log.i("Nicosanti", "Cerca")
        if (newText != null && newText != "") {
            isAccessingDatabase.value = true
            viewModelScope.launch { Dispatchers.Default
                currentQuotesArray.value = (originalArray.filter { it.quoteText.contains(newText) }).toTypedArray()
                isAccessingDatabase.value = false
            }
        }
        else currentQuotesArray.value = originalArray
    }

    private suspend fun findOriginalPosition(): Int {
        var originalPosition = 0
        val currentQuoteId = currentQuotesArray.value!![currentPosition].quoteId
        val currentBookId = currentQuotesArray.value!![currentPosition].bookId

        withContext(Dispatchers.Default) {
            while (currentQuoteId != originalArray[originalPosition].quoteId || currentBookId != originalArray[originalPosition].bookId) originalPosition += 1
        }

        return originalPosition
    }

}