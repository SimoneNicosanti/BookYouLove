package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.EndDate
import it.simone.bookyoulove.model.ReadingModel
import it.simone.bookyoulove.view.ENDED_BOOK_STATE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReadingViewModel(application: Application) : AndroidViewModel(application) {

    //Per Accesso DB
    private val myApp : Application = application
    private val readingModel = ReadingModel(myApp.applicationContext)

    //Gestione Lista dei Libri in Lettura

    private var currentBookIndex : Int = 0

    //Gestione Visualizzazione
    val isAccessingDatabase = MutableLiveData<Boolean>()                            //Permette di mostrare un DialogFragment con una rotella che gira nel caso di operazioni di accesso a DB
    //val currentShowBook = MutableLiveData<ShowedBookInfo?>()                             //Info relative al libro mostrato attualmente
    var changedReadingList = MutableLiveData<Boolean>(true)                   //Comunicazione al fragment che la lista di libri in lettura è stata modificata da qualche altro fragment
    val currentReadingBookArray = MutableLiveData<Array<ShowedBookInfo>>()

    /*
    fun getNextBook() {
        currentBookIndex = (currentBookIndex + 1) % currentReadingBookArray.size
        setShowedBook()
    }

    fun getPrevBook() {
        currentBookIndex = (currentBookIndex - 1)
        currentBookIndex = if (currentBookIndex < 0) (currentReadingBookArray.size - 1) else currentBookIndex
        setShowedBook()
    }

    private fun setShowedBook() {
        if (currentReadingBookArray.isEmpty()) currentShowBook.value = null
        else currentShowBook.value = currentReadingBookArray[currentBookIndex]
    }

    fun restartShowedBook() {
        currentBookIndex = 0
        loadReadingBookList()
    }*/


    fun loadReadingBookList() {
        isAccessingDatabase.value = true
        viewModelScope.launch {
            currentReadingBookArray.value = readingModel.getReadingBookArray()
            isAccessingDatabase.value = false
            changedReadingList.value = false
            //setShowedBook()
        }
    }


    fun readingUpdated(updated: Boolean) {
        changedReadingList.value = updated
    }


    fun notifyBookTerminated() {
        /*
            L'unico libro che può essere segnato come terminato è il corrente!!
            Quando viene chiamata dall'ending fragment, viene rimosso l'elemento corrente nell'array,
            elemento che nel mentre viene salvato come terminato nel DB, si reimposta l'indice e si aggiorna il currentArray.
            L'endingFragment invia il segnale di modifica dei terminati all'ended che si ricarica la lista
         */
        val supportList = currentReadingBookArray.value?.toMutableList()
        supportList?.removeAt(currentBookIndex)
        currentBookIndex = 0
        currentReadingBookArray.value = supportList?.toTypedArray()
        //setShowedBook()
    }

    fun setCurrentItemPosition(position: Int) {
        currentBookIndex = position
    }

}