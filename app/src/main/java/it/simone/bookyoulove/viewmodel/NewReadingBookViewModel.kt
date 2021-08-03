package it.simone.bookyoulove.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.BookSupport
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.model.NewReadingBookModel
import it.simone.bookyoulove.view.*
import kotlinx.coroutines.*
import java.time.Month
import java.time.format.TextStyle
import java.util.*



class NewReadingBookViewModel(application: Application): AndroidViewModel(application) {


    private val newReadingBookModel = NewReadingBookModel(AppDatabase.getDatabaseInstance(application.applicationContext))


    private var loadedTitle : String? = null
    private var loadedAuthor : String? = null
    private var loadedTime : Int = 0

    val currentBook = MutableLiveData<Book>()
    val currentAuthorArray = MutableLiveData<Array<String>>()
    val isAccessingDatabase = MutableLiveData<Boolean>()
    val canExit = MutableLiveData<Boolean>()


    init {
        //Utilizzo +1 perché in libreria Calendar i mesi partono da 0, mentre in Month partono da 1
        val startDate : StartDate
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        startDate = StartDate(day, month, year)

        currentBook.value = Book("",
            "",
            0,
            startDate,
            null,
            BookSupport(paperSupport = false, ebookSupport = false, audiobookSupport = false),
            "",
            0,
            null,
            "",
            READING_BOOK_STATE)
    }



    fun loadAuthorArray() {
        var authorArray : Array<String>
        isAccessingDatabase.value = true
        viewModelScope.launch {
            authorArray = newReadingBookModel.loadAuthorArrayFromDatabase()
            currentAuthorArray.value = authorArray
            isAccessingDatabase.value = false
        }
    }


    fun addNewBook() {
        isAccessingDatabase.value = true

        CoroutineScope(Dispatchers.Main).launch {
            if (loadedTitle != null) {
                //Sto in caso di modifica di un libro precedente
                if (loadedTitle != currentBook.value?.title || loadedAuthor != currentBook.value?.author) {
                    val bookToRemove = Book(
                        loadedTitle!!, loadedAuthor!!, loadedTime,
                        null,
                        null,
                        null,
                        "",
                        0,
                        null,
                        "",
                        READING_BOOK_STATE
                    )
                    newReadingBookModel.removeBookFromDatabase(bookToRemove)
                    newReadingBookModel.addNewBookInDatabase(currentBook.value!!)
                }

                else {
                    newReadingBookModel.updateReadingBookInDatabase(currentBook.value!!)
                }
            }

            else {
                newReadingBookModel.addNewBookInDatabase(currentBook.value!!)
            }

            isAccessingDatabase.value = false
            canExit.value = true
        }
    }


    fun loadReadingBookToModify(readingModifyTitle: String, readingModifyAuthor: String, readingModifyTime: Int) {
        isAccessingDatabase.value = true
        loadedTitle = readingModifyTitle
        loadedAuthor = readingModifyAuthor
        loadedTime = readingModifyTime
        viewModelScope.launch {
            currentBook.value = newReadingBookModel.loadReadingBookToModifyFromDatabase(readingModifyTitle, readingModifyAuthor, readingModifyTime)
            isAccessingDatabase.value = false
        }
    }


    fun updateTitle(text: CharSequence?) {
        currentBook.value?.title = text.toString()
    }

    fun updateAuthor(text: CharSequence?) {
        currentBook.value?.author = text.toString()
    }

    fun updateStartDate(startDateResult: StartDate) {
        currentBook.value?.startDate = startDateResult
    }

    fun updateCoverLink(coverLinkResult: String) {
        currentBook.value?.coverName = coverLinkResult
    }

    fun updatePages(pagesResult: Int) {
        currentBook.value?.pages = pagesResult
    }

    fun updateSupport(supportMap: Map<String, Boolean>) {
        currentBook.value?.support?.paperSupport = supportMap[PAPER_SUPPORT]!!
        currentBook.value?.support?.ebookSupport = supportMap[EBOOK_SUPPORT]!!
        currentBook.value?.support?.audiobookSupport = supportMap[AUDIOBOOK_SUPPORT]!!
    }
}