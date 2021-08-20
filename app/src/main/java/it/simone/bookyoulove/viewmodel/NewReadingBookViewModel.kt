package it.simone.bookyoulove.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.BookSupport
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.model.NewReadingBookModel
import it.simone.bookyoulove.view.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.*


const val GOOGLE_BOOK_API_WITH_ISBN_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:"

const val ISBN_INTERNET_ACCESS_ERROR = 1
const val ISBN_FIND_ITEM_ERROR = 2
const val ISBN_NO_ERROR = 0

class NewReadingBookViewModel(application: Application): AndroidViewModel(application) {


    private val newReadingBookModel = NewReadingBookModel(AppDatabase.getDatabaseInstance(application.applicationContext))


    val currentBook = MutableLiveData<Book>()
    val currentAuthorArray = MutableLiveData<Array<String>>()
    val isAccessingDatabase = MutableLiveData(false)
    val canExitWithBook = MutableLiveData<Book>()
    val internetAccessError = MutableLiveData(0)

    private var bookLoadedOnce = false
    private var authorsLoadedOnce = false

    private val modifyReadingRequestQueue = Volley.newRequestQueue(application.applicationContext)


    init {
        //Utilizzo +1 perché in libreria Calendar i mesi partono da 0, mentre in Month partono da 1
        val startDate : StartDate
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        startDate = StartDate(day, month, year)

        currentBook.value = Book(
            bookId = 0,
            "",
            "",
            startDate = startDate,
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
        Log.d("Nicosanti", "Added")
        isAccessingDatabase.value = true

        CoroutineScope(Dispatchers.Main).launch {
            if (currentBook.value!!.bookId != 0L) {
                //Sto in caso di modifica di un libro precedente
                newReadingBookModel.updateReadingBookInDatabase(currentBook.value!!)
                newReadingBookModel.changeQuotesInfoInDatabase(currentBook.value!!)
            }

            else {
                //Aggiunta nuovo libro
                newReadingBookModel.addNewBookInDatabase(currentBook.value!!)
            }

            isAccessingDatabase.value = false
            canExitWithBook.value = currentBook.value!!
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

    fun updatePages(pagesResult: String?) {
        currentBook.value?.pages = if (pagesResult == null || pagesResult == "") 0 else pagesResult.toInt()
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


    fun findBookByIsbn(scannedIsbn : String) {
        isAccessingDatabase.value = true
        val googleBookApiUrl = GOOGLE_BOOK_API_WITH_ISBN_URL + scannedIsbn
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, googleBookApiUrl, null,
                { response -> setNetworkResponseAsBook(response) },
                { _ ->
                    isAccessingDatabase.value = false
                    internetAccessError.value = ISBN_INTERNET_ACCESS_ERROR
                }
        )
        modifyReadingRequestQueue.add(jsonObjectRequest)
    }

    private fun setNetworkResponseAsBook(requestResponse: JSONObject?) {

        if (requestResponse != null && requestResponse.getInt("totalItems") != 0) {

            val requestedBookVolumeInfo = (requestResponse.getJSONArray("items")[0] as JSONObject).getJSONObject("volumeInfo")
            if (requestedBookVolumeInfo.has("title")) currentBook.value!!.title = requestedBookVolumeInfo.getString("title")
            if (requestedBookVolumeInfo.has("authors")) currentBook.value!!.author = requestedBookVolumeInfo.getJSONArray("authors")[0].toString()
            if (requestedBookVolumeInfo.has("pageCount")) currentBook.value!!.pages = requestedBookVolumeInfo.getInt("pageCount")
            if(requestedBookVolumeInfo.getJSONObject("imageLinks").has("thumbnail")) {
                var thumbnailLink = requestedBookVolumeInfo.getJSONObject("imageLinks").getString("thumbnail")
                if (!thumbnailLink.startsWith("https")) {
                    // Se non comincia con https comincia con http. Poiché i server di google non ammettono richieste http sostituisco il prefisso
                    thumbnailLink = "https" + thumbnailLink.removePrefix("http")
                    //Purtroppo non ci sono link che danno una qualità di immagine migliore
                }
                currentBook.value!!.coverName = thumbnailLink.toString()
            }

            currentBook.value = currentBook.value
            isAccessingDatabase.value = false
        }
        else {
            isAccessingDatabase.value = false
            internetAccessError.value = ISBN_FIND_ITEM_ERROR
        }

    }

    fun handledInternetError(hasInternetError : Int) {
        internetAccessError.value = hasInternetError
    }
}