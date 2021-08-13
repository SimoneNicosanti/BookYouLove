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


    val currentBook = MutableLiveData<Book>()
    val currentAuthorArray = MutableLiveData<Array<String>>()
    val isAccessingDatabase = MutableLiveData<Boolean>()
    val canExitWithBook = MutableLiveData<Book>()

    private var bookLoadedOnce = false
    private var authorsLoadedOnce = false

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
            title = "",
            author = "",
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
        if (!authorsLoadedOnce) {
            var authorArray: Array<String>
            isAccessingDatabase.value = true
            viewModelScope.launch {
                authorArray = newReadingBookModel.loadAuthorArrayFromDatabase()
                currentAuthorArray.value = authorArray
                isAccessingDatabase.value = false
            }
        }
    }


    fun addNewBook() {
        isAccessingDatabase.value = true

        CoroutineScope(Dispatchers.Main).launch {
            if (currentBook.value!!.keyTitle != "") {
                //Sto in caso di modifica di un libro precedente
                if (currentBook.value!!.keyTitle != newReadingBookModel.formatKeyInfo(currentBook.value?.title!!) ||
                    currentBook.value!!.keyAuthor != newReadingBookModel.formatKeyInfo(currentBook.value?.author!!)) {
                        //Sono stati modificati titolo e autore in qualcosa che modifica la chiave!!

                            val currentBookCopy = currentBook.value!!.copy()
                    newReadingBookModel.removeBookFromDatabase(currentBookCopy)
                    /*
                        Quando invocata la addNewBookInDatabase il parametro è passato per riferimento, quindi
                        sono impostati i campi della chiave direttamente nel current.value: posso quindi assegnare direttamente
                        lui come finalBook
                     */

                    newReadingBookModel.addNewBookInDatabase(currentBook.value!!)
                    newReadingBookModel.changeQuotesInfoInDatabase(currentBookCopy)
                }

                else {
                    // Anche se modificati titole e autore la chiave rimane uguale
                    newReadingBookModel.updateReadingBookInDatabase(currentBook.value!!)
                }
            }

            else {
                Log.i("Nicosanti", "${currentBook.value!!.keyTitle}")
                newReadingBookModel.addNewBookInDatabase(currentBook.value!!)
                Log.i("Nicosanti", "${currentBook.value!!.keyTitle}")
            }

            isAccessingDatabase.value = false
            canExitWithBook.value = currentBook.value!!
        }
    }

    /*
    fun loadReadingBookToModify(readingModifyKeyTitle: String, readingModifyKeyAuthor: String, readingModifyTime: Int) {
        isAccessingDatabase.value = true
        viewModelScope.launch {
            currentBook.value = newReadingBookModel.loadReadingBookToModifyFromDatabase(readingModifyKeyTitle, readingModifyKeyAuthor, readingModifyTime)
            isAccessingDatabase.value = false
        }
    }*/


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

    fun setBookToModify(readingModifyBook: Book) {
        if (!bookLoadedOnce) {
            currentBook.value = readingModifyBook
            bookLoadedOnce = true
        }
    }
}