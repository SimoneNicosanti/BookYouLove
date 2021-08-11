package it.simone.bookyoulove.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.model.DetailReadingModel
import it.simone.bookyoulove.view.READING_BOOK_STATE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Month
import java.time.format.TextStyle
import java.util.*

class DetailReadingViewModel(application : Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val detailReadingModel = DetailReadingModel(myAppDatabase)

    private var loadedOnce = false

    val isAccessingDatabase = MutableLiveData<Boolean>()
    val currentBook = MutableLiveData<Book>()

    fun loadDetailReadingBook(detailKeyTitle: String, detailKeyAuthor: String, detailTime: Int) {
        if (!loadedOnce) {
            isAccessingDatabase.value = true
            viewModelScope.launch {
                currentBook.value = detailReadingModel.loadDetailReadingBookFromDatabase(
                    detailKeyTitle,
                    detailKeyAuthor,
                    detailTime
                )
                loadedOnce = true
                isAccessingDatabase.value = false
            }
        }
    }
}