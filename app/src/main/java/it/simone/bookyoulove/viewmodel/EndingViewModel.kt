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

        /*
            Imposto la data di fine iniziale coincidente con la data di inizio.
            Questo mi permette di evitare situazioni patologiche sulle date, che si verificherebbero se impostassi
            a data odierna. Se ad esempio fosse messo come data di inizio una data successiva a quella odierna e poi si
            andasse a terminazione la data di fine sarebbe per default quella odierna: se questa non viene impostata da
            utente, ottengo endDate < startDate che è assurdo. Questo mi permette di evitarlo. Comunque il Picker parte da data
            odierna, quindi all'utente basta aprirlo per impostare la data di fine a quella del giorno corrente

         */
        loadedBook.endDate = EndDate(loadedBook.startDate!!.startDay, loadedBook.startDate!!.startMonth, loadedBook.startDate!!.startYear)
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


    fun loadEndingBook(endingKeyTitle: String, endingKeyAuthor: String, endingTime: Int) {
        if (!loadedOnce) {

            isAccessingDatabase.value = true
            viewModelScope.launch {
                loadedBook = endingModel.loadEndingBook(endingKeyTitle, endingKeyAuthor, endingTime)
                loadedOnce = true
                prepareTerminateBook()
                isAccessingDatabase.value = false
            }
        }
    }

}