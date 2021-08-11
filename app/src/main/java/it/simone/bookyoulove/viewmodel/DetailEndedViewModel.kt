package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.model.DetailEndedModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailEndedViewModel(application: Application) : AndroidViewModel(application) {


    private val myApp = application
    private val myAppDatabase = AppDatabase.getDatabaseInstance(myApp.applicationContext)
    private val detailEndedModel = DetailEndedModel(myAppDatabase)

    private var loadedOnce : Boolean = false



    val currentBook = MutableLiveData<Book>()
    val isAccessingDatabase = MutableLiveData<Boolean>()
    val deleteCompleted = MutableLiveData<Boolean>()


    fun deleteCurrentBook() {
        isAccessingDatabase.value = true
        CoroutineScope(Dispatchers.Main).launch {
            detailEndedModel.deleteBook(currentBook.value!!)
            isAccessingDatabase.value = false
            deleteCompleted.value = true
        }
    }

    fun loadEndedDetailBook(endedDetailKeyTitle: String, endedDetailKeyAuthor: String, endedDetailTime: Int) {
        if (!loadedOnce) {
            isAccessingDatabase.value = true
            viewModelScope.launch {
                currentBook.value = detailEndedModel.loadEndedDetailBook(endedDetailKeyTitle, endedDetailKeyAuthor, endedDetailTime)
                isAccessingDatabase.value = false
            }
        }
    }

    fun changeThought(newThought: String) {
        currentBook.value?.finalThought = newThought
    }
}
