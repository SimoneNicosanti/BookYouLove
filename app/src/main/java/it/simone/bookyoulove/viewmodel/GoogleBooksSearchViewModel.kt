package it.simone.bookyoulove.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.volley.toolbox.Volley
import it.simone.bookyoulove.Constants.ISBN_NO_ERROR
import it.simone.bookyoulove.Constants.TAG
import it.simone.bookyoulove.model.GoogleBooksApi

class GoogleBooksSearchViewModel(application: Application) : AndroidViewModel(application),
    GoogleBooksApi.OnGoogleBooksApiRequestTerminated {

    val isAccessing = MutableLiveData(false)
    val currentNetworkBookList = MutableLiveData(mutableListOf<GoogleBooksApi.NetworkBook>())
    val currentResponseCode = MutableLiveData<Int>()


    private val requestQueue = Volley.newRequestQueue(application.applicationContext)

    fun askBooksByTitle(query: String) {
        isAccessing.value = true
        GoogleBooksApi(this, requestQueue).findBookByTitle(query)
    }

    override fun onNetworkBookReceived(
        networkBook: GoogleBooksApi.NetworkBook?,
        responseCode: Int
    ) {}

    override fun onNetworkBookArrayReceived(
        networkBookList: MutableList<GoogleBooksApi.NetworkBook>,
        responseCode: Int
    ) {
        Log.d(TAG, "$responseCode")
        isAccessing.value = false
        if (responseCode == ISBN_NO_ERROR) {
            currentNetworkBookList.value = networkBookList
        }
        else {
            currentResponseCode.value = responseCode
            currentResponseCode.value = ISBN_NO_ERROR
        }
    }


}