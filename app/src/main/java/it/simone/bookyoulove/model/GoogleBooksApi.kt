package it.simone.bookyoulove.model


import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import it.simone.bookyoulove.viewmodel.ModifyBookViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

const val GOOGLE_BOOK_API_WITH_ISBN_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:"

const val ISBN_INTERNET_ACCESS_ERROR = 1
const val ISBN_FIND_ITEM_ERROR = 2
const val ISBN_NO_ERROR = 0


class GoogleBooksApi(modifyBookViewModel: ModifyBookViewModel, private val modifyBookRequestQueue: RequestQueue) {

    private val currentBook = modifyBookViewModel.currentBook
    private val isAccessing = modifyBookViewModel.isAccessing
    private val internetAccessError = modifyBookViewModel.internetAccessError

    private val viewModelScope = modifyBookViewModel.viewModelScope

    fun findBookByIsbn(scannedIsbn : String) {
        val googleBookApiUrl = GOOGLE_BOOK_API_WITH_ISBN_URL + scannedIsbn
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, googleBookApiUrl, null,
                { response -> setNetworkResponseAsBook(response) },
                {
                    isAccessing.value = false
                    internetAccessError.value = ISBN_INTERNET_ACCESS_ERROR
                }
        )
        modifyBookRequestQueue.add(jsonObjectRequest)
    }


    private fun setNetworkResponseAsBook(requestResponse: JSONObject?) {
        viewModelScope.launch(Dispatchers.Default) {
            if (requestResponse != null && requestResponse.getInt("totalItems") != 0) {

                val requestedBookVolumeInfo = (requestResponse.getJSONArray("items")[0] as JSONObject).getJSONObject("volumeInfo")
                if (requestedBookVolumeInfo.has("title")) currentBook.value!!.title = requestedBookVolumeInfo.getString("title")
                if (requestedBookVolumeInfo.has("authors")) currentBook.value!!.author = requestedBookVolumeInfo.getJSONArray("authors")[0].toString()
                if (requestedBookVolumeInfo.has("pageCount")) currentBook.value!!.pages = requestedBookVolumeInfo.getInt("pageCount")
                if (requestedBookVolumeInfo.has("imageLinks") && requestedBookVolumeInfo.getJSONObject("imageLinks").has("thumbnail")) {
                    var thumbnailLink = requestedBookVolumeInfo.getJSONObject("imageLinks").getString("thumbnail")
                    if (!thumbnailLink.startsWith("https")) {
                        // Se non comincia con https comincia con http. Poiché i server di google non ammettono richieste http sostituisco il prefisso
                        thumbnailLink = "https" + thumbnailLink.removePrefix("http")
                        //Purtroppo non ci sono link che danno una qualità di immagine migliore
                    }

                    currentBook.value!!.coverName = thumbnailLink.toString()
                }

                withContext(Dispatchers.Main) {
                    currentBook.value = currentBook.value
                    isAccessing.value = false
                }
            } else {
                withContext(Dispatchers.Main) {
                    isAccessing.value = false
                    internetAccessError.value = ISBN_FIND_ITEM_ERROR
                }
            }
        }

    }
}