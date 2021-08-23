package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.model.TbrModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TbrViewModel(application: Application) : AndroidViewModel(application) {

    val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val tbrModel = TbrModel(myAppDatabase)

    private val myApp = application

    val currentTbrArray = MutableLiveData(arrayOf<ShowedBookInfo>())
    val isAccessing = MutableLiveData(false)

    private var loadedOnce = false

    private var currentPosition = -1

    private var originalArray = arrayOf<ShowedBookInfo>()

    private var searchField : String? = null


    fun getTbrArray() {
        if (!loadedOnce) {
            viewModelScope.launch {
                isAccessing.value = true
                val loadedArray = tbrModel.loadTbrArrayFromDatabase()
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(myApp.applicationContext)
                val order = sharedPreferences.getString("tbrOrderPreference", "title")
                originalArray = tbrModel.sortTbrBookArrayByPreference(loadedArray, order)
                currentTbrArray.value = originalArray
                isAccessing.value = false
                loadedOnce = true
            }
        }
    }

    fun onNewTbrBookAdded(newTbrBook: Book?) {

        if (newTbrBook != null) {
            val newTbrShowedBookInfo = ShowedBookInfo(
                bookId = newTbrBook.bookId,
                title = newTbrBook.title,
                author = newTbrBook.author,
                coverName = newTbrBook.coverName,
                startDate = newTbrBook.startDate,
                endDate = newTbrBook.endDate,
                totalRate = newTbrBook.rate?.totalRate,
                pages = newTbrBook.pages
            )

            val supportArray = arrayOf(newTbrShowedBookInfo).plus(originalArray)
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(myApp.applicationContext)
            val order = sharedPreferences.getString("tbrOrderPreference", "title")
            isAccessing.value = true
            viewModelScope.launch {
                originalArray = tbrModel.sortTbrBookArrayByPreference(supportArray, order)
                currentTbrArray.value = originalArray
                isAccessing.value = false
                //onSearchQuery(searchField)
            }
        }
    }


    fun deleteTbrBook() {
        if (currentPosition != -1) {
            isAccessing.value = true
            CoroutineScope(Dispatchers.Main).launch {
                tbrModel.deleteTbrBook(currentTbrArray.value!![currentPosition].bookId)
                val originalPosition = findOriginalPosition()
                withContext(Dispatchers.Default) {
                    val supportMutableList = originalArray.toMutableList()
                    supportMutableList.removeAt(originalPosition)
                    originalArray = supportMutableList.toTypedArray()
                }
                currentTbrArray.value = originalArray
                isAccessing.value = false
                //onSearchQuery(searchField)
            }
        }
    }

    fun setItemPosition(position: Int) {
        currentPosition = position
    }

    fun onModifyTbrBook(tbrModifiedBook: Book?) {
        if (currentPosition != -1 && tbrModifiedBook != null) {
            val modifiedTbrShowedBookInfo = ShowedBookInfo(
                    bookId = tbrModifiedBook.bookId,

                    title = tbrModifiedBook.title,
                    author = tbrModifiedBook.author,
                    coverName = tbrModifiedBook.coverName,
                    startDate = tbrModifiedBook.startDate,
                    endDate = tbrModifiedBook.endDate,
                    totalRate = tbrModifiedBook.rate?.totalRate,
                    pages = tbrModifiedBook.pages
            )
            isAccessing.value = true
            viewModelScope.launch {
                val originalPosition = findOriginalPosition()
                originalArray[originalPosition] = modifiedTbrShowedBookInfo
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(myApp.applicationContext)
                val order = sharedPreferences.getString("tbrOrderPreference", "title")
                
                originalArray = tbrModel.sortTbrBookArrayByPreference(originalArray, order)
                currentTbrArray.value = originalArray
                isAccessing.value = false
                //onSearchQuery(searchField)
            }
        }
    }

    fun onStartedBook() {
        viewModelScope.launch {
            val originalPosition = findOriginalPosition()
            withContext(Dispatchers.Default) {
                val supportList = originalArray.toMutableList()
                supportList.removeAt(originalPosition)
                originalArray = supportList.toTypedArray()
            }
            currentTbrArray.value = originalArray
            //onSearchQuery(searchField)
        }
    }


    fun onSearchQuery(newText : String?) {
        searchField = newText
        if (newText == null || newText == "") {
            currentTbrArray.value = originalArray
        }
        else {
            isAccessing.value = true
            viewModelScope.launch {
                val filteredArray : Array<ShowedBookInfo>
                withContext(Dispatchers.Default) {
                    filteredArray = (originalArray.filter { it.title.contains(newText) || it.author.contains(newText) }).toTypedArray()
                }
                currentTbrArray.value = filteredArray
                isAccessing.value = false
            }
        }
    }

    private suspend fun findOriginalPosition(): Int {
        var originalPosition = 0
        withContext(Dispatchers.Default) {
            while(currentTbrArray.value!![currentPosition].bookId != originalArray[originalPosition].bookId) originalPosition += 1
        }
        return originalPosition
    }
}