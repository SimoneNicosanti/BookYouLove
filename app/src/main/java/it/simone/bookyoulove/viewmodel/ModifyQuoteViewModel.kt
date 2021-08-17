package it.simone.bookyoulove.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowQuoteInfo
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
    val isAccessingDatabase = MutableLiveData(false)
    val canExitWithQuote = MutableLiveData<Quote>()

    //private var loadedOnce = false

    private var loadedQuoteText = ""

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
        //Log.i("Nicosanti", "${newQuoteText.hashCode()}")
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
        //Potrei evitare di usare isAccessing, visto che alla fine una volta che torno indietro la quote è passata direttamente, ma in questo modo ho
        //la garanzia che se l'utente
        isAccessingDatabase.value = true
        CoroutineScope(Dispatchers.Main).launch {
            if (loadedQuoteText == "") {
                //Citazione ex-novo
                modifyQuoteModel.insertQuoteInDatabase(currentQuote.value!!)
            }

            else {
                if (loadedQuoteText == currentQuote.value!!.quoteText) {
                    //Non modificato il testo --> upadate semplice
                    modifyQuoteModel.updateQuoteInDatabase(currentQuote.value!!)
                }
                else {
                    //Modificato il testo --> devo eliminare e reinserire citazione visto che il testo è la chiave
                    modifyQuoteModel.deleteQuoteFromDatabase(currentQuote.value!!.copy(quoteText = loadedQuoteText))
                    modifyQuoteModel.insertQuoteInDatabase(currentQuote.value!!)
                }
            }
            isAccessingDatabase.value = false
            canExitWithQuote.value = currentQuote.value!!
        }
    }

    fun changeQuoteReadTime(readTime: Int) {
        currentQuote.value!!.readTime = readTime
    }

    /*
    fun getModifyQuote(modifyQuoteText: String, bookTitle: String, bookAuthor: String, bookReadTime: Int) {
        if (!loadedOnce) {
            val keyTitle = modifyQuoteModel.formatKeyInfo(bookTitle)
            val keyAuthor = modifyQuoteModel.formatKeyInfo(bookAuthor)
            viewModelScope.launch {
                currentQuote.value = modifyQuoteModel.loadQuoteToModifyFromDatabase(modifyQuoteText, keyTitle, keyAuthor, bookReadTime)
                loadedQuoteText = currentQuote.value!!.quoteText
                loadedOnce = true
            }
        }
    }*/

    fun setModifyQuote(modifyQuote: Quote) {
        currentQuote.value = modifyQuote
        loadedQuoteText = modifyQuote.quoteText
    }
}