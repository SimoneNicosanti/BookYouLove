package it.simone.bookyoulove.viewmodel.quotes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowQuoteInfo
import it.simone.bookyoulove.database.entity.Quote
import it.simone.bookyoulove.model.QuoteList
import it.simone.bookyoulove.model.quotes.QuoteListModel
import kotlinx.coroutines.launch

class QuoteListViewModel(application : Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val quoteListModel = QuoteListModel(myAppDatabase)

    val currentQuotesArray = MutableLiveData<MutableList<ShowQuoteInfo>>()
    val isAccessingDatabase = MutableLiveData(false)

    private lateinit var quoteList : QuoteList
    private var loadedOnce = false

    fun getAllQuotes() {
        if (!loadedOnce) {
            isAccessingDatabase.value = true
            viewModelScope.launch {
                val loadedArray = quoteListModel.loadAllQuotesFromDatabase()
                quoteList = QuoteList(loadedArray.toMutableList())
                loadedOnce = true
                currentQuotesArray.value = quoteList.getValue()
                isAccessingDatabase.value = false
            }
        }
    }

    fun getQuotesByBookId(bookId : Long) {
        if (!loadedOnce) {
            isAccessingDatabase.value = true
            viewModelScope.launch {
                val loadedArray = quoteListModel.loadQuotesByBookFromDatabase(bookId)
                quoteList = QuoteList(loadedArray.toMutableList())
                loadedOnce = true
                currentQuotesArray.value = quoteList.getValue()
                isAccessingDatabase.value = false
            }
        }
    }

    fun onQuoteDeleted() {
        isAccessingDatabase.value = true
        viewModelScope.launch {
            quoteList.onQuoteDeleted()
            currentQuotesArray.value = quoteList.getValue()
            isAccessingDatabase.value = false
        }
    }

    fun changeSelectedQuote(selectedQuote: ShowQuoteInfo) {
        quoteList.setSelectedQuote(selectedQuote)
    }

    fun onQuoteModified(modifiedQuote: Quote) {
        isAccessingDatabase.value = true
        viewModelScope.launch {
            quoteList.onModifiedQuote(modifiedQuote)
            currentQuotesArray.value = quoteList.getValue()
            isAccessingDatabase.value = false
        }
    }

}