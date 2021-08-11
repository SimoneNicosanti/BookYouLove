package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowQuoteInfo
import it.simone.bookyoulove.database.entity.Quote
import it.simone.bookyoulove.model.QuoteListModel
import kotlinx.coroutines.launch

class QuoteListViewModel(application : Application) : AndroidViewModel(application) {

    val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    val quoteListModel = QuoteListModel(myAppDatabase)

    val currentQuotesArray = MutableLiveData<Array<ShowQuoteInfo>>(arrayOf())

    val isAccessingDatabase = MutableLiveData(false)

    fun getAllQuotes() {
        isAccessingDatabase.value = true
        viewModelScope.launch {
            currentQuotesArray.value = quoteListModel.loadAllQuotesFromDatabase()
            isAccessingDatabase.value = false
        }
    }

    fun getQuotesByBook(bookKeyTitle: String, bookKeyAuthor: String, bookReadTime : Int) {
        isAccessingDatabase.value = true
        viewModelScope.launch {
            currentQuotesArray.value = quoteListModel.loadQuotesByBookFromDatabase(bookKeyTitle, bookKeyAuthor, bookReadTime)
            isAccessingDatabase.value = false
        }
    }


}