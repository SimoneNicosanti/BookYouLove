package it.simone.bookyoulove.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.model.EndedModel
import it.simone.bookyoulove.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class EndedViewModel(application: Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val readModel = EndedModel(myAppDatabase)
    private val myApp : Application = application
    //Lo preimposto in modo da non avere mai un null, ma al massimo un array vuoto
    private var loadedArray : Array<ShowedBookInfo> = arrayOf()

    var changedEndedArrayOrder : Boolean = true

    val isAccessingDatabase =  MutableLiveData<Boolean>()

    val currentReadList = MutableLiveData<Array<ShowedBookInfo>>()
    val changedEndedList = MutableLiveData<Boolean>(true)
    val currentSelectedBook = MutableLiveData<ShowedBookInfo>()
    var currentSelectedPosition : Int = -1

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

    fun setSelectedBook(selectedBook: ShowedBookInfo) {
        currentSelectedBook.value = selectedBook
    }


    fun sortBookArray(notSortedArray: Array<ShowedBookInfo> = loadedArray) {

        isAccessingDatabase.value = true
        //delay(5000)

        viewModelScope.launch {
            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(myApp.applicationContext)
            val order = sharedPreferences.getString("endedOrderPreference", "start_date")
            Log.i("Nicosanti", "$order")

            val sortedArray = when (order) {
                "start_date" -> readModel.sortByDate(notSortedArray, SORT_START_DATE)
                "end_date" -> readModel.sortByDate(notSortedArray, SORT_END_DATE)
                "title" -> readModel.sortByTitleOrAuthor(notSortedArray, SORT_BY_TITLE)
                "author" -> readModel.sortByTitleOrAuthor(notSortedArray, SORT_BY_AUTHOR)

                else -> notSortedArray
            }
            loadedArray = sortedArray
            currentReadList.value = sortedArray

            isAccessingDatabase.value = false
        }
    }

    fun filterArray(newText : String?, filterParam : Int) {
        if (newText == null || newText == "") {
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
                when (filterParam) {
                    SEARCH_BY_TITLE -> currentReadList.value = (loadedArray.filter {
                        it.keyTitle.contains(newText) }).toTypedArray()

                    SEARCH_BY_AUTHOR -> currentReadList.value = (loadedArray.filter {it.keyAuthor.contains(newText)}).toTypedArray()
                    else -> {
                        val searchRate = newText.toFloat()
                        currentReadList.value = (loadedArray.filter {it.totalRate == searchRate}).toTypedArray()
                    }
                }
                isAccessingDatabase.value = false
            }

        }
    }


    fun notifyArrayItemChanged(finalBook: ShowedBookInfo) {
        //L'unico elemento che può essere cambiato è quello selezionato correntemente
        loadedArray[currentSelectedPosition] = finalBook
        sortBookArray(loadedArray)
        //Dopo il riordino non so la posizione in cui è andato il libro, quindi resetto il il selectedPosition
        currentSelectedPosition = -1
    }

    fun notifyArrayItemDelete() {
        //L'unico che può essere eliminato è il corrente
        viewModelScope.launch { Dispatchers.Default
            val support = loadedArray.toMutableList()
            support.removeAt(currentSelectedPosition)
            loadedArray = support.toTypedArray()
            sortBookArray()
        }
    }


}