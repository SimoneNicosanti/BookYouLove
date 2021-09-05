package it.simone.bookyoulove.viewmodel.quotes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Quote
import it.simone.bookyoulove.model.quotes.ModifyQuoteModel
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

    private var loadedOnce = false

    init {
        val cal = Calendar.getInstance()

        currentQuote.value = Quote(
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
                date = cal.timeInMillis)
    }


    fun changeQuoteChapter(text: CharSequence?) {
        currentQuote.value!!.quoteChapter = text.toString()
    }

    fun changeQuotePage(newPageText: String) {
        //La massima lunghezza di newPageText è impostata a 5 nel layout. Se non pongo limite ho problemi di overflow nella rappresentazione
        currentQuote.value!!.quotePage = if (newPageText != "") newPageText.toInt() else 0
    }

    fun changeQuoteText(newQuoteText: String) {
        currentQuote.value?.quoteText = newQuoteText
        //Log.i("Nicosanti", "${newQuoteText.hashCode()}")
    }

    fun changeQuoteThought(newQuoteThought: String) {
        currentQuote.value!!.quoteThought = newQuoteThought
    }


    fun changeQuoteFavorite(isFavorite : Boolean) {
        currentQuote.value!!.favourite = isFavorite
    }

    fun saveQuote() {
        //Potrei evitare di usare isAccessing, visto che alla fine una volta che torno indietro la quote è passata direttamente, ma in questo modo ho
        //la garanzia che se l'utente
        isAccessingDatabase.value = true
        CoroutineScope(Dispatchers.Main).launch {
            if (currentQuote.value!!.quoteId != 0L) {
                //Modifica vecchia quote
                modifyQuoteModel.updateQuoteInDatabase(currentQuote.value!!)
            }
            else {
                //Aggiunta nuova quote
                modifyQuoteModel.insertQuoteInDatabase(currentQuote.value!!)
            }
            isAccessingDatabase.value = false
            canExitWithQuote.value = currentQuote.value!!
        }
    }




    fun setModifyQuote(modifyQuote: Quote) {
        if (!loadedOnce) {
            currentQuote.value = modifyQuote
            loadedOnce = true
        }
    }


    fun setQuoteBookInfo(bookId: Long, bookTitle: String, bookAuthor: String) {
        currentQuote.value!!.bookId = bookId
        currentQuote.value!!.bookTitle = bookTitle
        currentQuote.value!!.bookAuthor = bookAuthor
    }
}