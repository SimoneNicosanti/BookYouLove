package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.model.EndedModel
import it.simone.bookyoulove.view.ENDED_BOOK_STATE
import kotlinx.coroutines.launch

class EndedViewModel(application: Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val readModel = EndedModel(myAppDatabase)

    val isAccessingDatabase =  MutableLiveData<Boolean>()

    val currentReadList = MutableLiveData<Array<Book>>()
    val changedEndedList = MutableLiveData<Boolean>(true)
    val currentSelectedBook = MutableLiveData<Book>()


    fun getReadList(){
        isAccessingDatabase.value = true
        viewModelScope.launch {
            val notSortedArray = readModel.loadReadList(ENDED_BOOK_STATE)
            currentReadList.value = readModel.sortByStartDate(notSortedArray)
            isAccessingDatabase.value = false
        }
    }

    fun setEndedListChanged(changed : Boolean) {
        changedEndedList.value = changed
    }

    fun setSelectedBook(selectedBook: Book) {
        currentSelectedBook.value = selectedBook
    }
}