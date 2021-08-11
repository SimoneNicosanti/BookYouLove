package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Quote
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.model.QuoteDetailModel
import kotlinx.coroutines.launch

class QuoteDetailViewModel(application : Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val quoteDetailModel = QuoteDetailModel(myAppDatabase)

    val currentQuote = MutableLiveData( Quote (
            quoteText = "",
            keyTitle = "",
            keyAuthor = "",
            readTime = 0,
            bookTitle = "",
            bookAuthor = "",
            favourite = false,
            toWidget = false,
            quotePage = 0,
            quoteChapter = "",
            quoteThought = "",
            date = StartDate(0,0,0)
    ))

    fun getSingleQuote(detailQuoteText: String, detailQuoteBookTitle: String, detailQuoteBookAuthor: String) {
        //isAccessing
        viewModelScope.launch {
            currentQuote.value = quoteDetailModel.loadSingleQuotFromDatabase(detailQuoteText, detailQuoteBookTitle, detailQuoteBookAuthor)
        }
    }
}