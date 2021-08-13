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
    var changedReadingList = MutableLiveData<Boolean>(true)                   //Comunicazione al fragment che la lista di libri in lettura è stata modificata da qualche altro fragment
    val currentReadingBookArray = MutableLiveData<Array<ShowedBookInfo>>()


    fun loadReadingBookList() {
        isAccessingDatabase.value = true
        viewModelScope.launch {
            currentReadingBookArray.value = readingModel.getReadingBookArray()
            isAccessingDatabase.value = false
            changedReadingList.value = false
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
    }

    fun setCurrentItemPosition(position: Int) {
        currentBookIndex = position
    }


    fun notifyReadingBookModified(modifiedBook : Book) {
        val modifiedShowedBookInfo = ShowedBookInfo(
                keyTitle = modifiedBook.keyTitle,
                keyAuthor = modifiedBook.keyAuthor,
                readTime = modifiedBook.readTime,
                title = modifiedBook.title,
                author = modifiedBook.author,
                coverName = modifiedBook.coverName,
                startDate = modifiedBook.startDate,
                endDate = modifiedBook.endDate,
                totalRate = modifiedBook.rate?.totalRate
        )
        currentReadingBookArray.value!![currentBookIndex] = modifiedShowedBookInfo
        currentBookIndex = 0
    }

    fun notifyNewReadingBook(newBook: Book) {
        val newShowedBookInfo = ShowedBookInfo(
                keyTitle = newBook.keyTitle,
                keyAuthor = newBook.keyAuthor,
                readTime = newBook.readTime,
                title = newBook.title,
                author = newBook.author,
                coverName = newBook.coverName,
                startDate = newBook.startDate,
                endDate = newBook.endDate,
                totalRate = newBook.rate?.totalRate
        )
        val supportList = currentReadingBookArray.value!!.toMutableList()
        supportList.add(newShowedBookInfo)
        currentReadingBookArray.value = supportList.toTypedArray()
        currentBookIndex = 0
    }
}