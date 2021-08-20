package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.compose.runtime.key
import androidx.core.os.persistableBundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
    val tbrModel = TbrModel(myAppDatabase)

    val currentTbrArray = MutableLiveData(arrayOf<ShowedBookInfo>())
    val isAccessing = MutableLiveData(false)

    private var loadedOnce = false

    private var currentPosition = -1

    fun getTbrArray() {
        if (!loadedOnce) {
            viewModelScope.launch {
                isAccessing.value = true
                currentTbrArray.value = tbrModel.loadTbrArrayFromDatabase()
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
                totalRate = newTbrBook.rate?.totalRate
            )

            currentTbrArray.value = arrayOf(newTbrShowedBookInfo).plus(currentTbrArray.value!!)
        }
    }

    fun deleteTbrBook() {
        if (currentPosition != -1) {
            isAccessing.value = true
            CoroutineScope(Dispatchers.Main).launch {
                tbrModel.deleteTbrBook(currentTbrArray.value!![currentPosition].bookId)
                withContext(Dispatchers.Main) {
                    val supportMutableList = currentTbrArray.value!!.toMutableList()
                    supportMutableList.removeAt(currentPosition)
                    currentTbrArray.value = supportMutableList.toTypedArray()
                }
                isAccessing.value = false
                currentPosition = -1
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
                    totalRate = tbrModifiedBook.rate?.totalRate
            )
            currentTbrArray.value!![currentPosition] = modifiedTbrShowedBookInfo
            currentPosition = -1
        }
    }
}