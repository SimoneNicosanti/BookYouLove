package it.simone.bookyoulove.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.model.DetailReadingModel
import it.simone.bookyoulove.view.READING_BOOK_STATE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Month
import java.time.format.TextStyle
import java.util.*

class DetailReadingViewModel(application : Application) : AndroidViewModel(application) {


    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val detailReadingModel = DetailReadingModel(myAppDatabase)

    private var loadedTitle = ""
    private var loadedAuthor = ""
    private var loadedTime = 0

    var newPages: Int = 0
    var coverLink: String = ""
    var startDate: StartDate? = null

    var showedBook: Book? = null

    private lateinit var loadedBook: Book


    val isAccessingDatabase = MutableLiveData<Boolean>()

    // Utilizzata per stabire quando il fragment può uscire invocando onBackPress() : viene osservata e ne viene gestito solo il valore true
    //val canExit = MutableLiveData<Boolean>()
    val currentBook = MutableLiveData<Book>()
    val loadedDataOnce = MutableLiveData<Boolean>()
    val currentPages = MutableLiveData<Int>()
    val currentStartDate = MutableLiveData<String>()
    val currentCover = MutableLiveData<String>()
    val updatedDatabase = MutableLiveData<Boolean>()

    init {
        loadedDataOnce.value = false
    }

    fun updatePages() {
        currentPages.value = newPages
        currentBook.value?.pages = newPages
    }

    fun updateTitle() {
        currentBook.value?.title = showedBook?.title.toString()
    }

    fun updateAuthor() {
        currentBook.value?.author = showedBook?.author.toString()
    }

    fun updateStartDate() {
        if (startDate == null) currentStartDate.value = R.string.not_inserted_string.toString()
        else currentStartDate.value = "${startDate!!.startDay} ${Month.of(startDate!!.startMonth).getDisplayName(TextStyle.FULL, Locale.getDefault())} ${startDate!!.startYear}"
        currentBook.value?.startDate = startDate
    }

    fun updateCoverLink() {
        currentCover.value = coverLink
        currentBook.value?.coverName = coverLink
    }

    fun updateSupport() {
        currentBook.value?.support = showedBook?.support
    }

    fun updateBook() {
        currentBook.value = showedBook
    }


    fun getRequestedBook(requestedTitle: String, requestedAuthor: String, requestedTime: Int) {
        isAccessingDatabase.value = true
        viewModelScope.launch {

            loadedBook = detailReadingModel.getBookFromDatabase(requestedTitle, requestedAuthor, requestedTime)
            loadedTitle = loadedBook.title
            loadedAuthor = loadedBook.author
            loadedTime = loadedBook.readTime

            currentCover.value = loadedBook.coverName
            currentPages.value = if (loadedBook.pages != null) loadedBook.pages else 0

            startDate = loadedBook.startDate
            updateStartDate()

            currentBook.value = loadedBook

            isAccessingDatabase.value = false
            loadedDataOnce.value = true

            /*
                Mi permette di creare una nuova istanza di Book con gli stessi valori di quello caricato. Non posso operare direttamente su
                loadedBook perché devo mantenerne una copia qualora debba eliminarlo dal database. L'assegnazione nelle classi è in termini di puntatori
            */
            showedBook = loadedBook
            Log.i("Nicosanti", "${loadedBook.title} ${loadedBook.author} ${loadedBook.readTime}")
        }
    }

    fun saveModifiedBook() {
        Log.i("Nicosanti", "Saving")
        isAccessingDatabase.value = true
        currentBook.value?.let {
            if (it.title != loadedTitle || it.author != loadedAuthor) {
                CoroutineScope(Dispatchers.Main).launch {
                    //Poiché è cambiata la chiave, elimino la riga precedente del libro dal DB
                    val deleteBook = Book(loadedTitle, loadedAuthor, loadedTime, null, null, null, "", null, null, null, READING_BOOK_STATE)
                    Log.i("Nicosanti", "${deleteBook.title} ${deleteBook.author} ${deleteBook.readTime}")
                    detailReadingModel.removeBookFromDatabase(deleteBook)
                    detailReadingModel.addBookInDatabase(it)
                    isAccessingDatabase.value = false
                    updatedDatabase.value = true
                }
            }
            else {
                CoroutineScope(Dispatchers.Main).launch {
                    detailReadingModel.updateBookInDatabase(it)
                    isAccessingDatabase.value = false
                    updatedDatabase.value = true
                }
            }
        }
    }
}