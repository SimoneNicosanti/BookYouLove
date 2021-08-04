package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
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

    var loadedOnce : Boolean = false

    val currentBook = MutableLiveData<Book>()
    val canExit = MutableLiveData<Boolean>(false)
    val isAccessingDatabase = MutableLiveData<Boolean>()

    lateinit var currentStartDate : StartDate
    lateinit var currentEndDate : EndDate

    private var modified = false

    lateinit var finalBook : ShowedBookInfo


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

    fun modifyRate(newRate: Float, modifiedRate: Int) {
        if (!modified) modified = true

        when (modifiedRate) {
            0 -> currentBook.value?.rate?.totalRate = newRate
            1 -> currentBook.value?.rate?.styleRate = newRate
            2 -> currentBook.value?.rate?.emotionRate = newRate
            3 -> currentBook.value?.rate?.plotRate = newRate
            4 -> currentBook.value?.rate?.characterRate = newRate
        }

    }


    fun saveModifiedBook() {
        /*
            Poiché sto non sto sul viewModelScope, e poichè non è necessario attendere che il valore
            sia caricato nel DB affinché compaia nella lista, posso evitare l'udo di isAccessing
         */
        if (modified) {
            val modifiedBook = currentBook.value!!
            finalBook = ShowedBookInfo(modifiedBook.title, modifiedBook.author, modifiedBook.readTime, modifiedBook.coverName, modifiedBook.startDate, modifiedBook.endDate, modifiedBook.rate?.totalRate)
            //isAccessingDatabase.value = true
            CoroutineScope(Dispatchers.Main).launch {
                modifyEndedModel.saveChangedBook(currentBook.value!!)
                //isAccessingDatabase.value = false
            }
            canExit.value = true
        }
    }


    fun setBookToModify(modifyEndedTitle: String, modifyEndedAuthor: String, modifyEndedTime: Int) {
        if (!loadedOnce) {
            isAccessingDatabase.value = true
            viewModelScope.launch {
                currentBook.value = modifyEndedModel.loadEndedBookToModify(
                    modifyEndedTitle,
                    modifyEndedAuthor,
                    modifyEndedTime
                )
                currentStartDate = currentBook.value!!.startDate!!
                currentEndDate = currentBook.value!!.endDate!!
                loadedOnce = true
                isAccessingDatabase.value = false
            }
        }
    }



}