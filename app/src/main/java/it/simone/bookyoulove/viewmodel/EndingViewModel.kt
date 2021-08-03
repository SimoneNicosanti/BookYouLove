package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.EndDate
import it.simone.bookyoulove.database.entity.Rate
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.model.EndingModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class EndingViewModel(application: Application) : AndroidViewModel(application) {

    var isFlipped = false
    private var loadedOnce = false
    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val endingModel = EndingModel(myAppDatabase)

    val terminateBook = MutableLiveData<Book>()
    val isAccessingDatabase = MutableLiveData<Boolean>()
    val canExit = MutableLiveData<Boolean>()

    private lateinit var loadedBook : Book

    private fun prepareTerminateBook() {
        loadedBook.rate = Rate(0F,0F,0F,0F,0F)

        val cal = Calendar.getInstance()
        loadedBook.endDate = EndDate(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
        loadedBook.finalThought = ""
        terminateBook.value = loadedBook
    }

    fun modifyTotalRate(rating: Float) {
        terminateBook.value?.rate?.totalRate = rating
    }

    fun modifyStyleRate(rating: Float) {
        terminateBook.value?.rate?.styleRate = rating
    }

    fun modifyEmotionsRate(rating: Float) {
        terminateBook.value?.rate?.emotionRate = rating
    }

    fun modifyPlotRate(rating: Float) {
        terminateBook.value?.rate?.plotRate = rating
    }

    fun modifyCharactersRate(rating: Float) {
        terminateBook.value?.rate?.characterRate = rating
    }

    fun setFinalThought(text: CharSequence?) {
        terminateBook.value?.finalThought = text.toString()
    }

    fun setEndDate(settedEndDate: EndDate) {
        terminateBook.value?.endDate = settedEndDate
    }


    fun saveTerminatedBook() {
        isAccessingDatabase.value = true
        CoroutineScope(Dispatchers.Main).launch {
            endingModel.saveTerminatedBook(terminateBook.value)
            isAccessingDatabase.value = false
            canExit.value = true
        }
    }


    fun loadEndingBook(endingTitle: String, endingAuthor: String, endingTime: Int) {
        if (!loadedOnce) {

            isAccessingDatabase.value = true
            viewModelScope.launch {
                loadedBook = endingModel.loadEndingBook(endingTitle, endingAuthor, endingTime)
                loadedOnce = true
                prepareTerminateBook()
                isAccessingDatabase.value = false
            }
        }
    }


}