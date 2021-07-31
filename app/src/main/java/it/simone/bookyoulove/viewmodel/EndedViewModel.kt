package it.simone.bookyoulove.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.model.EndedModel
import it.simone.bookyoulove.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EndedViewModel(application: Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val readModel = EndedModel(myAppDatabase)
    private val myApp : Application = application
    private lateinit var loadedArray : Array<Book>

    var changedEndedArrayOrder : Boolean = true

    val isAccessingDatabase =  MutableLiveData<Boolean>()

    val currentReadList = MutableLiveData<Array<Book>>()
    val changedEndedList = MutableLiveData<Boolean>(true)
    val currentSelectedBook = MutableLiveData<Book>()


    fun getEndedList() {
        isAccessingDatabase.value = true
        viewModelScope.launch {
            loadedArray = readModel.loadReadList(ENDED_BOOK_STATE)
            isAccessingDatabase.value = false
            sortBookArray(loadedArray)
        }
    }


    fun setEndedListChanged(changed : Boolean) {
        changedEndedList.value = changed
    }

    fun setSelectedBook(selectedBook: Book) {
        currentSelectedBook.value = selectedBook
    }


    suspend fun sortBookArray(notSortedArray: Array<Book>? = loadedArray) {

        isAccessingDatabase.value = true
        //delay(5000)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(myApp.applicationContext)
        val order = sharedPreferences.getString("endedOrderPreference", "start_date")
        Log.i("Nicosanti", "$order")

        if (notSortedArray != null) {
            val sortedArray = when (order) {
                "start_date" -> readModel.sortByDate(notSortedArray, SORT_START_DATE)
                "end_date" -> readModel.sortByDate(notSortedArray, SORT_START_DATE)
                "title" -> readModel.sortByTitleOrAuthor(notSortedArray, SORT_BY_TITLE)
                "author" -> readModel.sortByTitleOrAuthor(notSortedArray, SORT_BY_AUTHOR)

                else -> notSortedArray
            }
            loadedArray = sortedArray
            currentReadList.value = sortedArray
        }

        isAccessingDatabase.value = false
    }

    fun filterArray(newText : String?) {
        if (newText == null) {
            currentReadList.value = loadedArray
        }
        else {
            Log.i("Nicosanti", "Ricerca")
            /*
                Mi serve il casting perché la filter ritorna una List anziché un Array.
                Poiché il filtraggio può essere un'operazione lunga, lancio una coroutine e nel mentre
                blocco l'interfaccia usando il Loading Dialog
             */
            isAccessingDatabase.value = true
            viewModelScope.launch {
                currentReadList.value = (loadedArray.filter { it.title.toLowerCase().contains(newText) }).toTypedArray()
                isAccessingDatabase.value = false
            }

        }
    }
}