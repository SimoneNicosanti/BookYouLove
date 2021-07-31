package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book

class DetailEndedViewModel(application: Application) : AndroidViewModel(application) {


    private val myApp = application
    private val myAppDatabase = AppDatabase.getDatabaseInstance(myApp.applicationContext)

    var loadedOnce : Boolean = false
    var showedBook : Book? = null



    val currentBook = MutableLiveData<Book>()
    val isAccessingDatabase = MutableLiveData<Boolean>()
    val currentStartDate = MutableLiveData<String>()
    val isEditing = MutableLiveData<Boolean>(false)


    fun setShowedBook() {
        currentBook.value = showedBook
        showBothDates()
    }

    private fun showBothDates() {

    }
}