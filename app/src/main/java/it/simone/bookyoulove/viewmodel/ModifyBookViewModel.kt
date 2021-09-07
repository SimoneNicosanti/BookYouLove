package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.volley.toolbox.Volley
import it.simone.bookyoulove.Constants.AUDIOBOOK_SUPPORT
import it.simone.bookyoulove.Constants.EBOOK_SUPPORT
import it.simone.bookyoulove.Constants.PAPER_SUPPORT
import it.simone.bookyoulove.Constants.READING_BOOK_STATE
import it.simone.bookyoulove.Constants.TBR_BOOK_STATE
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.BookSupport
import it.simone.bookyoulove.model.GoogleBooksApi
import it.simone.bookyoulove.model.ModifyBookModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class ModifyBookViewModel(application: Application) : AndroidViewModel(application), GoogleBooksApi.OnGoogleBooksApiRequestTerminated {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val modifyBookModel = ModifyBookModel(myAppDatabase)

    private var loadedOnce = false
    private var authorsLoadedOnce = false
    private val modifyBookRequestQueue = Volley.newRequestQueue(application.applicationContext)

    val currentBook = MutableLiveData<Book>()
    val isAccessing = MutableLiveData(false)
    val canExitWithBook = MutableLiveData<Book>()
    val internetAccessError = MutableLiveData<Int>()
    val currentAuthorArray = MutableLiveData<Array<String>>(arrayOf())

    fun prepareNewBook(bookState : Int) {
        if (!loadedOnce) {
            val modifyBook: Book

            if (bookState == TBR_BOOK_STATE) {
                modifyBook = Book(
                        bookId = 0,
                        title = "",
                        author = "",
                        startDate = null,
                        endDate = null,
                        support = null,
                        coverName = "",
                        pages = 0,
                        rate = null,
                        finalThought = "",
                        readState = TBR_BOOK_STATE
                )
            }
            else {
                //Reading
                val cal = Calendar.getInstance()

                modifyBook = Book(
                        bookId = 0,
                        "",
                        "",
                        startDate = cal.timeInMillis,
                        null,
                        support = BookSupport(paperSupport = false, ebookSupport = false, audiobookSupport = false),
                        "",
                        0,
                        null,
                        "",
                        READING_BOOK_STATE)
            }
            loadedOnce = true
            currentBook.value = modifyBook
        }
    }


    fun setBookToModify(modifyEndedBook : Book) {
        if (!loadedOnce) {
            currentBook.value = modifyEndedBook
            loadedOnce = true
        }
    }


    fun loadAuthorArray() {
        if (!authorsLoadedOnce) {
            var authorArray: Array<String>
            isAccessing.value = true
            viewModelScope.launch {
                authorArray = modifyBookModel.loadAuthorArrayFromDatabase()
                currentAuthorArray.value = authorArray
                authorsLoadedOnce = true
                isAccessing.value = false
            }
        }
    }

    fun addNewBook() {
        isAccessing.value = true

        CoroutineScope(Dispatchers.Main).launch {
            if (currentBook.value!!.bookId != 0L) {
                //Sto in caso di modifica di un libro precedente
                modifyBookModel.updateBookInDatabase(currentBook.value!!)
            }

            else {
                //Aggiunta nuovo libro
                modifyBookModel.addNewBookInDatabase(currentBook.value!!)
            }

            isAccessing.value = false
            canExitWithBook.value = currentBook.value!!
        }
    }



    fun modifyStartDate(newStartDate: Long) {
        currentBook.value?.startDate = newStartDate
    }

    fun modifyEndDate(newEndDate: Long) {
        currentBook.value?.endDate = newEndDate
    }

    fun modifyPages(newPagesString: String?) {
        currentBook.value?.pages = if (newPagesString == null || newPagesString == "") 0 else newPagesString.toInt()
    }

    fun modifyCover(newCoverLink : String) {
        currentBook.value?.coverName = newCoverLink
    }

    fun modifySupport(newSupportMap : Map<String , Boolean>) {
        currentBook.value?.support?.paperSupport = newSupportMap[PAPER_SUPPORT]!!
        currentBook.value?.support?.ebookSupport = newSupportMap[EBOOK_SUPPORT]!!
        currentBook.value?.support?.audiobookSupport = newSupportMap[AUDIOBOOK_SUPPORT]!!
    }

    fun modifyRate(newRate: Float, modifiedRate: Int) {

        when (modifiedRate) {
            0 -> currentBook.value?.rate?.totalRate = newRate
            1 -> currentBook.value?.rate?.styleRate = newRate
            2 -> currentBook.value?.rate?.emotionRate = newRate
            3 -> currentBook.value?.rate?.plotRate = newRate
            4 -> currentBook.value?.rate?.characterRate = newRate
        }

    }

    fun modifyTitle(text: CharSequence?) {
        currentBook.value!!.title = text?.toString() ?: ""
    }

    fun modifyAuthor(text: CharSequence?) {
        currentBook.value!!.author = text?.toString() ?: ""
    }


    fun askBookByIsbn(scannedIsbn : String) {
        isAccessing.value = true
        GoogleBooksApi(this, modifyBookRequestQueue).findBookByIsbn(scannedIsbn)
    }

    fun handledInternetError(hasInternetError : Int) {
        internetAccessError.value = hasInternetError
    }

    fun getTbrModifyBook(bookId: Long) {
        if (!loadedOnce) {
            isAccessing.value = true
            viewModelScope.launch {
                val loadedBook : Book
                withContext(Dispatchers.IO) {
                    loadedBook = myAppDatabase.bookDao().loadBookById(bookId)
                }
                currentBook.value = loadedBook
                isAccessing.value = false
            }
        }
    }


    override fun onNetworkBookReceived(
        networkBook: GoogleBooksApi.NetworkBook?,
        responseCode: Int) {
        isAccessing.value = false
        if (networkBook == null) {
            internetAccessError.value = responseCode
        }
        else {
            currentBook.value!!.title = networkBook.title
            currentBook.value!!.author = networkBook.authors
            currentBook.value!!.pages = networkBook.pageCount
            currentBook.value!!.coverName = networkBook.thumbnail

            currentBook.value = currentBook.value
        }
    }

    override fun onNetworkBookArrayReceived(
        networkBookList: MutableList<GoogleBooksApi.NetworkBook>,
        responseCode: Int
    ) {}
}