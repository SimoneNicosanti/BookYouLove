package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
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

    var loadedOnce : Boolean = false
    var showedBook : Book? = null



    val currentBook = MutableLiveData<Book>()
    val isAccessingDatabase = MutableLiveData<Boolean>()
    val currentStartDate = MutableLiveData<String>()
    val isEditing = MutableLiveData<Boolean>(false)

    val deleteCompleted = MutableLiveData<Boolean>()

    fun setShowedBook() {
        currentBook.value = showedBook
    }


    fun deleteCurrentBook() {
        isAccessingDatabase.value = true
        CoroutineScope(Dispatchers.Main).launch {
            detailEndedModel.deleteBook(showedBook!!)
            isAccessingDatabase.value = false
            deleteCompleted.value = true
        }
    }
}