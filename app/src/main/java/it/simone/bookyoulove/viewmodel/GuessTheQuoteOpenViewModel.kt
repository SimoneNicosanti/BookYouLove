package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GuessTheQuoteOpenViewModel(application: Application) : AndroidViewModel(application) {

    val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)

    val canPlay = MutableLiveData<Boolean>()
    val isAccessing = MutableLiveData(false)

    var loadedOnce = false

    fun canPlayVerify() {
        if (!loadedOnce) {
            isAccessing.value = true
            viewModelScope.launch {
                val bookIdsList : Array<Long>
                val quoteIdsList : Array<Long>

                withContext(Dispatchers.IO) {
                    bookIdsList = myAppDatabase.bookDao().loadBookKeys()
                    quoteIdsList = myAppDatabase.quoteDao().loadQuoteKeys()
                }

                isAccessing.value = false
                canPlay.value = bookIdsList.size >= 3 && quoteIdsList.size >= 10
                loadedOnce = true
            }
        }
    }
}