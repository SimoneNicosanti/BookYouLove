package it.simone.bookyoulove.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Quote
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.model.ModifyQuoteModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ModifyQuoteViewModel(application: Application) : AndroidViewModel(application) {

    val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val modifyQuoteModel = ModifyQuoteModel(myAppDatabase)

    val currentQuote = MutableLiveData<Quote>()

    init {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        currentQuote.value = Quote(
                quoteText = "",
                keyTitle = "",
                readTime = 0,
                keyAuthor = "",
                bookTitle = "",
                bookAuthor = "",
                favourite = false,
                toWidget = false,
                quotePage = 0,
                quoteChapter = "",
                quoteThought = "",
                date = StartDate(startDay = day, startMonth = month, startYear = year))
    }


    fun changeQuoteChapter(text: CharSequence?) {
        currentQuote.value!!.quoteChapter = text.toString()
    }

    fun changeQuotePage(newPageText: String) {
        //LA massima lunghezza di newPageText è impostata a 5 nel layout. Se non pongo limite ho problemi di overflow nella rappresentazione
        currentQuote.value!!.quotePage = if (newPageText != "") newPageText.toInt() else 0
    }

    fun changeQuoteText(newQuoteText: String) {
        currentQuote.value?.quoteText = newQuoteText
        Log.i("Nicosanti", currentQuote.value!!.quoteText)
    }

    fun changeQuoteThought(newQuoteThought: String) {
        currentQuote.value!!.quoteThought = newQuoteThought
    }

    fun changeQuoteTitle(bookTitle: String) {
        //Da titolo --> Chiave, ma non posso fare viceversa!!
        currentQuote.value!!.bookTitle = bookTitle
        currentQuote.value!!.keyTitle = modifyQuoteModel.formatKeyInfo(bookTitle)
    }

    fun changeQuoteAuthor(bookAuthor: String) {
        currentQuote.value!!.bookAuthor = bookAuthor
        currentQuote.value!!.keyAuthor = modifyQuoteModel.formatKeyInfo(bookAuthor)
    }

    fun changeQuoteFavorite(isFavorite : Boolean) {
        currentQuote.value!!.favourite = isFavorite
    }

    fun saveQuote() {
        CoroutineScope(Dispatchers.Main).launch {
            modifyQuoteModel.insertQuoteInDatabase(currentQuote.value!!)
        }
    }

    fun changeQuoteReadTime(readTime: Int) {
        currentQuote.value!!.readTime = readTime
    }
}