package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.Constants.AUDIOBOOK_SUPPORT
import it.simone.bookyoulove.Constants.EBOOK_SUPPORT
import it.simone.bookyoulove.Constants.PAPER_SUPPORT
import it.simone.bookyoulove.Constants.READING_BOOK_STATE
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.BookSupport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class StartingViewModel(application: Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val startingModel = StartingModel(myAppDatabase)

    private var loadedOnce = false

    val currentStartingBook = MutableLiveData<Book>()
    val canExitWithBook = MutableLiveData<Book>()
    val isAccessing = MutableLiveData(false)

    fun loadStartingBook(bookId: Long) {
        if (!loadedOnce) {
            isAccessing.value = true
            viewModelScope.launch {
                val loadedBook = startingModel.loadBookFromDatabase(bookId)
                currentStartingBook.value = prepareStartingBook(loadedBook)
                isAccessing.value = false
            }
        }
    }

    private fun prepareStartingBook(loadedBook: Book): Book {
        val cal = Calendar.getInstance()
        loadedBook.startDate = cal.timeInMillis
        loadedBook.support = BookSupport(
            paperSupport = false,
            ebookSupport = false,
            audiobookSupport = false,
        )
        loadedBook.readState = READING_BOOK_STATE

        return loadedBook
    }

    fun setBookAsStarted() {
        isAccessing.value = true
        CoroutineScope(Dispatchers.Main).launch {
            startingModel.setBookAsStartedInDatabase(currentStartingBook.value!!)
            isAccessing.value = false
            canExitWithBook.value = currentStartingBook.value
        }
    }


    fun changeStartDate(newStartDate: Long) {
        currentStartingBook.value!!.startDate = newStartDate
    }

    fun changeSupport(supportMap : Map<String, Boolean>) {
        currentStartingBook.value!!.support!!.paperSupport = supportMap[PAPER_SUPPORT]!!
        currentStartingBook.value!!.support!!.ebookSupport = supportMap[EBOOK_SUPPORT]!!
        currentStartingBook.value!!.support!!.audiobookSupport = supportMap[AUDIOBOOK_SUPPORT]!!
    }
}

class StartingModel(private val myAppDatabase: AppDatabase) {

    suspend fun loadBookFromDatabase(startingBookId: Long): Book {
        val loadedBook : Book
        withContext(Dispatchers.IO) {
            loadedBook = myAppDatabase.bookDao().loadBookById(startingBookId)
        }
        return loadedBook
    }

    suspend fun setBookAsStartedInDatabase(startingBook: Book) {
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().updateBooks(startingBook)
        }
    }

}