package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.EndDate
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.model.ModifyEndedModel
import it.simone.bookyoulove.view.AUDIOBOOK_SUPPORT
import it.simone.bookyoulove.view.EBOOK_SUPPORT
import it.simone.bookyoulove.view.PAPER_SUPPORT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ModifyEndedViewModel(application : Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val modifyEndedModel = ModifyEndedModel(myAppDatabase)

    var receivedOnce : Boolean = false

    lateinit var selectedBook : Book

    val currentBook = MutableLiveData<Book>()
    val canExit = MutableLiveData<Boolean>(false)
    val isAccessingDatabase = MutableLiveData<Boolean>()

    lateinit var currentStartDate : StartDate
    lateinit var currentEndDate : EndDate

    private var modified = false

    var finalBook : Book? = null

    fun postSelectedBook() {
        /*
            Utilizzo Una copia perché i dati sono passati per riferimento!! se non lo facessi, qualora l'utente
            uscisse senza aver salvato le modifiche se le ritorverebbe comunque nella lista dei visualizzati
         */
        currentBook.value = selectedBook.copy()
        currentStartDate = currentBook.value!!.startDate!!
    }

    fun modifyStartDate(newStartDate : StartDate) {
        if (!modified) modified = true
        currentStartDate = newStartDate
        currentBook.value?.startDate = newStartDate
    }

    fun modifyEndDate(newEndDate : EndDate) {
        if (!modified) modified = true
        currentEndDate = newEndDate
        currentBook.value?.endDate = newEndDate
    }

    fun modifyPages(newPages : Int) {
        if (!modified) modified = true
        currentBook.value?.pages = newPages
    }

    fun modifyCover(newCoverLink : String) {
        if (!modified) modified = true
        currentBook.value?.coverName = newCoverLink
    }

    fun modifySupport(newSupportMap : Map<String , Boolean>) {
        if (!modified) modified = true
        currentBook.value?.support?.paperSupport = newSupportMap[PAPER_SUPPORT]!!
        currentBook.value?.support?.ebookSupport = newSupportMap[EBOOK_SUPPORT]!!
        currentBook.value?.support?.audiobookSupport = newSupportMap[AUDIOBOOK_SUPPORT]!!
    }

    fun modifyRate(newRate: Float) {
        if (!modified) modified = true
        currentBook.value?.rate = newRate
    }


    fun saveModifiedBook() {
        /*
            Poiché sto non sto sul viewModelScope, e poichè non è necessario attendere che il valore
            sia caricato nel DB affinché compaia nella lista, posso evitare l'udo di isAccessing
         */
        if (modified) {
            finalBook = currentBook.value
            //isAccessingDatabase.value = true
            CoroutineScope(Dispatchers.Main).launch {
                modifyEndedModel.saveChangedBook(currentBook.value!!)
                //isAccessingDatabase.value = false
                canExit.value = true
            }
        }
    }
}