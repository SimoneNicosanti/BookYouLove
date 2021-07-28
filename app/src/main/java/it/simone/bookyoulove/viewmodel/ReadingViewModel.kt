package it.simone.bookyoulove.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
    private lateinit var currentReadingBookArray : Array<Book>
    private var currentBookIndex : Int = 0

    //Gestione Visualizzazione
    val isAccessingDatabase = MutableLiveData<Boolean>()                            //Permette di mostrare un DialogFragment con una rotella che gira nel caso di operazioni di accesso a DB
    val currentShowBook = MutableLiveData<Book?>()                             //Info relative al libro mostrato attualmente
    var changedReadingList = MutableLiveData<Boolean>(true)                   //Comunicazione al fragment che la lista di libri in lettura è stata modificata da qualche altro fragment
    val markedAsEnded = MutableLiveData<Boolean>(false)                         //Mi permette di dire al fragment di marcare come modificata la lista degli ended, in modo che quando l'endedFragment la legge la richiede in DB


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
    }


    fun loadReadingBookList() {
        isAccessingDatabase.value = true
        Log.i("SIMONE_NICOSANTI", "ReadingVM : Lettura Lista da DB")
        viewModelScope.launch {
            currentReadingBookArray = readingModel.getReadingBookArray()
            isAccessingDatabase.value = false
            setShowedBook()
        }
    }

    fun terminateBook(endDate : EndDate, settedRate : Float) {
        isAccessingDatabase.value = true
        CoroutineScope(Dispatchers.Main).launch {
            if (currentShowBook.value != null) {
                val bookInfo = currentShowBook.value!!
                val termBook = readingModel.loadTermBook(bookInfo)
                termBook.endDate = endDate
                termBook.rate = settedRate
                termBook.readState = ENDED_BOOK_STATE
                readingModel.saveTerminatedBook(termBook)
            }
            isAccessingDatabase.value = false
            changedReadingList.value = true
            markedAsEnded.value = true
        }
    }


    fun readingUpdated(updated: Boolean) {
        Log.i("Nicosanti", " Nuovo stato $updated")
        changedReadingList.value = updated
    }

    fun changeNotified() {
        markedAsEnded.value = false
    }

}