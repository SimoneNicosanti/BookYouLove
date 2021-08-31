package it.simone.bookyoulove.viewmodel.quotes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Quote
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.model.quotes.QuoteDetailModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuoteDetailViewModel(application : Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val quoteDetailModel = QuoteDetailModel(myAppDatabase)

    private var loadedOnce = false

    val currentQuote = MutableLiveData( Quote (
            quoteId = 0L,
            bookId = 0L,
            quoteText = "",
            bookTitle = "",
            bookAuthor = "",
            favourite = false,
            toWidget = false,
            quotePage = 0,
            quoteChapter = "",
            quoteThought = "",
            date = StartDate(0,0,0)
    ))

    fun getSingleQuote(quoteId: Long, bookId: Long) {
        //isAccessing
        if (!loadedOnce) {
            viewModelScope.launch {
                currentQuote.value = quoteDetailModel.loadSingleQuotFromDatabase(quoteId, bookId)
                loadedOnce = true
            }
        }
    }

    fun deleteCurrentQuote() {
        CoroutineScope(Dispatchers.Main).launch {
            quoteDetailModel.deleteCurrentQuote(currentQuote.value!!)
        }
    }


    fun onQuoteModified(modifiedQuote: Quote?) {
        currentQuote.value = modifiedQuote
    }
}