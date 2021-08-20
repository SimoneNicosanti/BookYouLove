package it.simone.bookyoulove.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowQuoteInfo
import it.simone.bookyoulove.model.QuoteListModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuoteListViewModel(application : Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val quoteListModel = QuoteListModel(myAppDatabase)

    val currentQuotesArray = MutableLiveData<Array<ShowQuoteInfo>>(arrayOf())
    val isAccessingDatabase = MutableLiveData(false)
    val currentSearchField = MutableLiveData<String>()

    private var currentPosition : Int = -1

    private var originalArray : Array<ShowQuoteInfo> = arrayOf()

    fun getAllQuotes() {
        isAccessingDatabase.value = true
        viewModelScope.launch {
            originalArray = quoteListModel.loadAllQuotesFromDatabase()
            currentQuotesArray.value = originalArray
            isAccessingDatabase.value = false
        }
    }

    fun getQuotesByBookId(bookId : Long) {
        isAccessingDatabase.value = true
        viewModelScope.launch {
            currentQuotesArray.value = quoteListModel.loadQuotesByBookFromDatabase(bookId)
            originalArray = currentQuotesArray.value!!
            isAccessingDatabase.value = false
        }
    }

    fun onQuoteDeleted() {
        val supportArrayList = currentQuotesArray.value!!.toMutableList()
        supportArrayList.removeAt(currentPosition)
        currentPosition = -1
        currentQuotesArray.value = supportArrayList.toTypedArray()
    }

    fun setCurrentPosition(position: Int) {
        currentPosition = position
    }

    fun onModifiedQuote(showQuoteInfo: ShowQuoteInfo) {
        currentQuotesArray.value!![currentPosition] = showQuoteInfo
        currentQuotesArray.value = currentQuotesArray.value
        currentPosition = -1
    }

    fun searchByContents(newText: String?) {
        currentSearchField.value = newText ?: ""
        Log.i("Nicosanti", "Cerca")
        if (newText != null) {
            isAccessingDatabase.value = true
            viewModelScope.launch { Dispatchers.Default
                currentQuotesArray.value = (originalArray.filter { it.quoteText.contains(newText) }).toTypedArray()
                isAccessingDatabase.value = false
            }
        }
        else currentQuotesArray.value = originalArray
    }


}