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
import kotlinx.coroutines.withContext

class EndedViewModel(application: Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val readModel = EndedModel(myAppDatabase)
    private val myApp : Application = application
    private var sortType = ""

    //Lo preimposto in modo da non avere mai un null, ma al massimo un array vuoto
    private var loadedArray : Array<ShowedBookInfo> = arrayOf()

    private var filterText : String = ""
    private var filterType : Int = SEARCH_BY_TITLE

    var changedEndedArrayOrder : Boolean = true

    val isAccessingDatabase =  MutableLiveData(false)
    val currentReadList = MutableLiveData<Array<ShowedBookInfo>>()
    private var changedEndedList = true

    var currentSelectedPosition : Int = -1

    fun getEndedList() {
        if(changedEndedList) {
            //Se l'array è cambiato lo ricarico e lo riordino
            Log.d("Nicosanti", "Modificato")
            isAccessingDatabase.value = true
            viewModelScope.launch {
                loadedArray = readModel.loadEndedList(ENDED_BOOK_STATE)
                isAccessingDatabase.value = false
                changedEndedList = false
                sortBookArray(loadedArray)
            }
        }

        else {
            //Se l'array è rimasto uguale allora eseguo SOLO il riordino se è cambiato il tipo di ordinamento
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(myApp.applicationContext)
            val preferenceSortType = sharedPreferences.getString("endedOrderPreference", "start_date")
            if (sortType != preferenceSortType && sortType != "") {
                Log.d("Nicosanti", "Riordino")
                sortType = preferenceSortType.toString()
                sortBookArray(loadedArray)
            }
        }
    }


    fun setEndedListChanged(changed : Boolean) {
        changedEndedList = changed
    }


    fun sortBookArray(notSortedArray: Array<ShowedBookInfo> = loadedArray) {

        isAccessingDatabase.value = true

        viewModelScope.launch {
            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(myApp.applicationContext)
            val order = sharedPreferences.getString("endedOrderPreference", "start_date")
            Log.i("Nicosanti", "$order")
            sortType = order.toString()
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
            //filterArray(searchField, searchType)
        }
    }

    fun filterArray(newText : String?, filterParam : Int) {
        filterText = newText!!
        filterType = filterParam
        Log.d("Nicosanti", "Making Filter $filterText $filterType")
        if (newText == "") {
            currentReadList.value = loadedArray
        }
        else {
            /*
                Mi serve il casting perché la filter ritorna una List anziché un Array.
                Poiché il filtraggio può essere un'operazione lunga, lancio una coroutine e nel mentre
                blocco l'interfaccia usando il Loading Dialog
             */
            isAccessingDatabase.value = true
            viewModelScope.launch {
                when (filterParam) {
                    SEARCH_BY_TITLE -> currentReadList.value = (loadedArray.filter {
                        it.title.contains(newText) }).toTypedArray()

                    SEARCH_BY_AUTHOR -> currentReadList.value = (loadedArray.filter {it.author.contains(newText)}).toTypedArray()
                    SEARCH_BY_RATE -> {
                        val searchRate = newText.toFloat()
                        currentReadList.value = (loadedArray.filter {it.totalRate == searchRate || it.totalRate == searchRate + 0.5F }).toTypedArray()
                    }
                    else -> {
                        val searchYear = newText.toInt()
                        currentReadList.value = (loadedArray.filter {it.startDate!!.startYear == searchYear || it.endDate!!.endYear == searchYear}).toTypedArray()
                    }
                }
                isAccessingDatabase.value = false
            }

        }
    }


    fun notifyArrayItemChanged(modifiedBook: Book) {
        //L'unico elemento che può essere cambiato è quello selezionato correntemente
        if (currentSelectedPosition != -1) {
            val modifiedShowBookInfo = ShowedBookInfo(
                modifiedBook.bookId,
                modifiedBook.title,
                modifiedBook.author,
                modifiedBook.coverName,
                modifiedBook.startDate,
                modifiedBook.endDate,
                modifiedBook.rate?.totalRate,
                modifiedBook.pages
            )
            viewModelScope.launch {
                val originalPosition = findOriginalPosition()
                loadedArray[originalPosition] = modifiedShowBookInfo
                sortBookArray(loadedArray)
                //filterArray(searchField, searchType)
                //Dopo il riordino non so la posizione in cui è andato il libro, quindi resetto il selectedPosition
                //currentSelectedPosition = -1
            }
        }
    }

    fun notifyArrayItemDelete() {
        //L'unico che può essere eliminato è il corrente
        if (currentSelectedPosition != -1) {
            viewModelScope.launch {
                val originalPosition = findOriginalPosition()
                withContext(Dispatchers.Default) {
                    val support = loadedArray.toMutableList()
                    support.removeAt(originalPosition)
                    loadedArray = support.toTypedArray()
                }
                sortBookArray()
                //filterArray(searchField, searchType)
                currentSelectedPosition = -1
            }
        }
    }

    fun resetSearchParams() {
        filterText = ""
        filterType = SEARCH_BY_TITLE
        Log.d("Nicosanti", "Reset")
    }

    private suspend fun findOriginalPosition() : Int {
        var originalPosition = 0

        withContext(Dispatchers.Default) {
            while (currentReadList.value!![currentSelectedPosition].bookId != loadedArray[originalPosition].bookId) originalPosition += 1
        }

        return originalPosition
    }
}