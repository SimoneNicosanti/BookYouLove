package it.simone.bookyoulove.viewmodel.reading

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.model.reading.DetailReadingModel
import kotlinx.coroutines.launch

class DetailReadingViewModel(application : Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val detailReadingModel = DetailReadingModel(myAppDatabase)

    private var loadedOnce = false

    val isAccessingDatabase = MutableLiveData(false)
    val currentBook = MutableLiveData<Book>()

    fun loadDetailReadingBook(detailBookId: Long) {
        if (!loadedOnce) {
            isAccessingDatabase.value = true
            viewModelScope.launch {
                currentBook.value = detailReadingModel.loadDetailReadingBookFromDatabase(detailBookId)
                loadedOnce = true
                isAccessingDatabase.value = false
            }
        }
    }

    fun onReadingBookModified(modifiedBook: Book) {
        currentBook.value = modifiedBook
    }
}