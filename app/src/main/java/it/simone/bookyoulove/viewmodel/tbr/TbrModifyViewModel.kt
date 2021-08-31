package it.simone.bookyoulove.viewmodel.tbr

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.model.tbr.TbrModifyModel
import it.simone.bookyoulove.view.TBR_BOOK_STATE
import it.simone.bookyoulove.viewmodel.reading.GOOGLE_BOOK_API_WITH_ISBN_URL
import it.simone.bookyoulove.viewmodel.reading.ISBN_FIND_ITEM_ERROR
import it.simone.bookyoulove.viewmodel.reading.ISBN_INTERNET_ACCESS_ERROR
import it.simone.bookyoulove.viewmodel.reading.ISBN_NO_ERROR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class TbrModifyViewModel(application: Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)

    private val tbrModifyModel = TbrModifyModel(myAppDatabase)



    private var loadedBookOnce = false
    private var loadedAuthorArrayOnce = false

    private val tbrModifyRequestQueue = Volley.newRequestQueue(application.applicationContext)


    val isAccessing = MutableLiveData(false)
    val currentAuthorArray = MutableLiveData(arrayOf<String>())
    val currentTbrBook = MutableLiveData(Book(
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
    ))
    val canExitWithBook = MutableLiveData<Book>()
    val internetAccessError = MutableLiveData(ISBN_NO_ERROR)


    fun getTbrModifyBook(tbrBookId : Long) {
        if (!loadedBookOnce) {
            isAccessing.value = true
            viewModelScope.launch {
                currentTbrBook.value = tbrModifyModel.loadTbrModifyBookFromDatabase(tbrBookId)
                isAccessing.value = false
                loadedBookOnce = true
            }
        }
    }


    fun loadAuthorList() {
        if (!loadedAuthorArrayOnce) {
            isAccessing.value = true
            viewModelScope.launch {
                currentAuthorArray.value = tbrModifyModel.loadAuthorArrayFromDatabase()
                isAccessing.value = false
            }
        }
    }

    fun changeTitleText(text: CharSequence?) {
        if (text != null) currentTbrBook.value!!.title = text.toString()
        else currentTbrBook.value!!.title = ""
    }

    fun changeAuthorText(text: CharSequence?) {
        if (text != null) currentTbrBook.value!!.author = text.toString()
        else currentTbrBook.value!!.author = ""
    }

    fun changePages(text: CharSequence?) {
        if (text != null && text.toString() != "") currentTbrBook.value!!.pages = text.toString().toInt()
        else currentTbrBook.value!!.pages = 0
    }

    fun saveTbrBook() {
        isAccessing.value = true
        CoroutineScope(Dispatchers.Main).launch {
            if (currentTbrBook.value!!.bookId != 0L) {
                //Modifica Libro Precedente
                tbrModifyModel.updateTbrBookInDatabase(currentTbrBook.value!!)
            }
            else {
                //Aggiunta libro
                tbrModifyModel.saveTbrBookInDatabase(currentTbrBook.value!!)
            }
            isAccessing.value = false
            canExitWithBook.value = currentTbrBook.value
        }
    }

    fun changeCoverLink(coverLink: String) {
        currentTbrBook.value!!.coverName = coverLink
    }

    fun findBookByIsbn(scannedIsbn : String) {
        isAccessing.value = true
        val googleBookApiUrl = GOOGLE_BOOK_API_WITH_ISBN_URL + scannedIsbn
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, googleBookApiUrl, null,
                { response -> setNetworkResponseAsBook(response) },
                {
                    isAccessing.value = false
                    internetAccessError.value = ISBN_INTERNET_ACCESS_ERROR
                }
        )
        tbrModifyRequestQueue.add(jsonObjectRequest)
    }

    private fun setNetworkResponseAsBook(requestResponse: JSONObject?) {

        if (requestResponse != null && requestResponse.getInt("totalItems") != 0) {

            val requestedBookVolumeInfo = (requestResponse.getJSONArray("items")[0] as JSONObject).getJSONObject("volumeInfo")
            if (requestedBookVolumeInfo.has("title")) currentTbrBook.value!!.title = requestedBookVolumeInfo.getString("title")
            if (requestedBookVolumeInfo.has("authors")) currentTbrBook.value!!.author = requestedBookVolumeInfo.getJSONArray("authors")[0].toString()
            if (requestedBookVolumeInfo.has("pageCount")) currentTbrBook.value!!.pages = requestedBookVolumeInfo.getInt("pageCount")
            if(requestedBookVolumeInfo.has("imageLinks") && requestedBookVolumeInfo.getJSONObject("imageLinks").has("thumbnail")) {
                var thumbnailLink = requestedBookVolumeInfo.getJSONObject("imageLinks").getString("thumbnail")
                if (!thumbnailLink.startsWith("https")) {
                    // Se non comincia con https comincia con http. Poiché i server di google non ammettono richieste http sostituisco il prefisso
                    thumbnailLink = "https" + thumbnailLink.removePrefix("http")
                    //Purtroppo non ci sono link che danno una qualità di immagine migliore
                }
                currentTbrBook.value!!.coverName = thumbnailLink.toString()
            }

            currentTbrBook.value = currentTbrBook.value
            isAccessing.value = false
        }
        else {
            isAccessing.value = false
            internetAccessError.value = ISBN_FIND_ITEM_ERROR
        }

    }

    fun handledInternetError(hasInternetError : Int) {
        internetAccessError.value = hasInternetError
    }
}