package it.simone.bookyoulove.model


import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import it.simone.bookyoulove.Constants.ISBN_FIND_ITEM_ERROR
import it.simone.bookyoulove.Constants.ISBN_INTERNET_ACCESS_ERROR
import it.simone.bookyoulove.Constants.ISBN_NO_ERROR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.Serializable




class GoogleBooksApi(private val googleBooksApiTerminatedListener: OnGoogleBooksApiRequestTerminated, private val volleyRequestQueue: RequestQueue) {

    companion object {
        const val GOOGLE_BOOK_API_WITH_ISBN_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:"
        const val GOOGLE_BOOK_API_WITH_TITLE_URL = "https://www.googleapis.com/books/v1/volumes?q=intitle:"
        const val GOOGLE_BOOK_API_MAX_RESULT = "&maxResults=40" //Massimo numero di risultati che possono essere ricevuti

    }

    fun findBookByTitle(titleQuery : String) {
        val urlTitle = titleQuery.replace(" ", "+")
        val googleBooksApiUrl = GOOGLE_BOOK_API_WITH_TITLE_URL + urlTitle + GOOGLE_BOOK_API_MAX_RESULT
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, googleBooksApiUrl, null,
            { response -> getNetworkBookArray(response) },
            { googleBooksApiTerminatedListener.onNetworkBookArrayReceived(mutableListOf(), ISBN_INTERNET_ACCESS_ERROR) }
        )
        volleyRequestQueue.add(jsonObjectRequest)
    }

    private fun getNetworkBookArray(response: JSONObject?) {
        CoroutineScope(Dispatchers.Default).launch {
            val networkBookList = mutableListOf<NetworkBook>()
            if (response != null && response.getInt("totalItems") != 0) {
                val responseArray = response.getJSONArray("items")
                for (itemIndex in 0 until responseArray.length()) {
                    val networkBook = NetworkBook()
                    parseGoogleBookApiBook(networkBook, response, itemIndex)
                    networkBookList.add(networkBook)
                }
                withContext(Dispatchers.Main) {
                    googleBooksApiTerminatedListener.onNetworkBookArrayReceived(
                        networkBookList,
                        ISBN_NO_ERROR
                    )
                }
            }
            else {
                withContext(Dispatchers.Main) {
                    googleBooksApiTerminatedListener.onNetworkBookArrayReceived(
                        mutableListOf(),
                        ISBN_FIND_ITEM_ERROR
                    )
                }
            }
        }
    }

    fun findBookByIsbn(scannedIsbn: String) {

        val googleBookApiUrl = GOOGLE_BOOK_API_WITH_ISBN_URL + scannedIsbn
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, googleBookApiUrl, null,
            { response -> setNetworkResponseAsBook(response) },
            {
                googleBooksApiTerminatedListener.onNetworkBookReceived(null, ISBN_INTERNET_ACCESS_ERROR)
            }
        )
        volleyRequestQueue.add(jsonObjectRequest)
    }


    private fun setNetworkResponseAsBook(requestResponse: JSONObject?) {

        CoroutineScope(Dispatchers.Default).launch {
            if (requestResponse != null && requestResponse.getInt("totalItems") != 0) {
                val networkBook = NetworkBook()

                parseGoogleBookApiBook(networkBook, requestResponse, 0)

                withContext(Dispatchers.Main) {
                    googleBooksApiTerminatedListener.onNetworkBookReceived(networkBook, ISBN_NO_ERROR)
                }
            }
            else {
                withContext(Dispatchers.Main) {
                    googleBooksApiTerminatedListener.onNetworkBookReceived(null, ISBN_FIND_ITEM_ERROR)
                }
            }
        }
    }

    private fun parseGoogleBookApiBook(networkBook: NetworkBook, requestResponse: JSONObject, itemIndex : Int) {
        val requestedBookVolumeInfo = (requestResponse.getJSONArray("items")[itemIndex] as JSONObject).getJSONObject("volumeInfo")
        if (requestedBookVolumeInfo.has("title")) networkBook.title = requestedBookVolumeInfo.getString("title")
        if (requestedBookVolumeInfo.has("authors")) networkBook.authors = requestedBookVolumeInfo.getJSONArray("authors")[0].toString()
        if (requestedBookVolumeInfo.has("pageCount")) networkBook.pageCount = requestedBookVolumeInfo.getInt("pageCount")
        if (requestedBookVolumeInfo.has("imageLinks") && requestedBookVolumeInfo.getJSONObject("imageLinks").has("thumbnail")) {
            var thumbnailLink = requestedBookVolumeInfo.getJSONObject("imageLinks").getString("thumbnail")
            if (!thumbnailLink.startsWith("https")) {
                // Se non comincia con https comincia con http. Poiché i server di google non ammettono richieste http sostituisco il prefisso
                thumbnailLink = "https" + thumbnailLink.removePrefix("http")
                //Purtroppo non ci sono link che danno una qualità di immagine migliore
            }

            networkBook.thumbnail = thumbnailLink.toString()
        }
    }


    interface OnGoogleBooksApiRequestTerminated {
        fun onNetworkBookReceived(networkBook: NetworkBook?, responseCode: Int)
        fun onNetworkBookArrayReceived(networkBookList : MutableList<NetworkBook>, responseCode: Int)
    }

    data class NetworkBook(
        var title: String = "",
        var authors: String = "",
        var pageCount: Int = 0,
        var thumbnail: String = ""
    ) : Serializable
}