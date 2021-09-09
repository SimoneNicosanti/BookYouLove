package it.simone.bookyoulove.viewmodel

import android.app.Application
import android.app.PendingIntent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import it.simone.bookyoulove.Constants.ENDED_BOOK_STATE
import it.simone.bookyoulove.Constants.READING_BOOK_STATE
import it.simone.bookyoulove.Constants.TBR_BOOK_STATE
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.model.BookList
import it.simone.bookyoulove.model.BookListModel
import kotlinx.coroutines.launch


class BookListViewModel(application: Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val bookListModel = BookListModel(myAppDatabase)
    private val myApp : Application = application


    val isAccessing =  MutableLiveData(false)
    val currentBookList = MutableLiveData<MutableList<ShowedBookInfo>>()

    private var loadedOnce = false

    private lateinit var bookList : BookList


    fun getTbrList() {
        getRequestedList(TBR_BOOK_STATE)
    }

    fun getReadingList() {
        getRequestedList(READING_BOOK_STATE)
    }

    fun getEndedList() {
        getRequestedList(ENDED_BOOK_STATE)
    }


    private fun getRequestedList(requestedState : Int) {
        if(!loadedOnce) {
            isAccessing.value = true
            viewModelScope.launch {
                val loadedArray = bookListModel.loadRequestedShownBookInfo(requestedState)
                bookList = BookList(loadedArray.toMutableList())
                isAccessing.value = false
                loadedOnce = true
                sortBookArray(requestedState)
            }
        }
    }


    private fun sortBookArray(requestedState: Int) {
        isAccessing.value = true

        viewModelScope.launch {

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(myApp.applicationContext)
            val sortType = when (requestedState) {
                ENDED_BOOK_STATE -> sharedPreferences.getString("endedOrderPreference", "end_date")
                TBR_BOOK_STATE -> sharedPreferences.getString("tbrOrderPreference", "title")
                else -> "start_date" //Nel caso in cui il chiamante sia reading ordino per default in base alla data di inizio: utilizzo un default perché non ho impostazione da utente
            }

            sortType?.let { bookList.sortBookList(it) }
            currentBookList.value = bookList.getValue()
            isAccessing.value = false
        }
    }


    fun notifyArrayItemChanged(modifiedBook: Book) {
        //L'unico elemento che può essere cambiato è quello selezionato correntemente
        isAccessing.value = true
        viewModelScope.launch {
            bookList.onArrayItemChanged(modifiedBook)
            currentBookList.value = bookList.getValue()
            isAccessing.value = false
        }
    }

    fun notifyArrayItemDelete(toDelete : Boolean) {
        //L'unico che può essere eliminato è il corrente
        isAccessing.value = true
        viewModelScope.launch {
            if (toDelete) {
                //Se è anche da cancellare lo cancello dal DB
                val selectedItem = bookList.getSelectedItem()
                selectedItem?.let { bookListModel.deleteBook(it.bookId) }
            }
            //Comunque poi lo rimuovo dalla lista attuale
            bookList.onArrayItemRemoved()
            currentBookList.value = bookList.getValue()
            isAccessing.value = false
        }
    }

    fun notifyNewArrayItem(newItem : Book) {
        isAccessing.value = true
        viewModelScope.launch {
            bookList.onNewBookAdded(newItem)
            currentBookList.value = bookList.getValue()
            isAccessing.value = false
        }
    }

    fun notifyBookMove() {
        val movedItem = bookList.getSelectedItem()
        if (movedItem != null) {
            isAccessing.value = true
            viewModelScope.launch {
                bookListModel.moveReadingBookToTbr(movedItem.bookId)
                bookList.onArrayItemRemoved()
                currentBookList.value = bookList.getValue()
                isAccessing.value = false
            }
        }
    }


    fun changeSelectedItem(item: ShowedBookInfo) {
        bookList.setItem(item)
    }

}