package it.simone.bookyoulove.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.model.NewReadingBookModel
import it.simone.bookyoulove.view.*
import kotlinx.coroutines.*
import java.time.Month
import java.time.format.TextStyle
import java.util.*



class NewReadingBookViewModel(application: Application): AndroidViewModel(application) {

    private val myApp = application
    private val newReadingBookModel = NewReadingBookModel(application.applicationContext)


    lateinit var newBookTitle: String
    lateinit var newBookAuthor: String

    var newBookStartDate : StartDate

    var newBookCoverLink: String = ""

    var newBookSupport: MutableMap<String, Boolean> = mutableMapOf(PAPER_SUPPORT to false, EBOOK_SUPPORT to false, AUDIOBOOK_SUPPORT to false)
    var newBookPages: Int = MIN_PAGES_AMOUNT


    private var readState: Int = READING_BOOK_STATE


    val currentTitle = MutableLiveData<String>()
    val currentAuthor = MutableLiveData<String>()
    val currentStartDateString = MutableLiveData<String>()
    val currentLink = MutableLiveData<String>()
    val currentSupport = MutableLiveData<String>()
    val currentPages = MutableLiveData<Int>()

    val currentAuthorArray = MutableLiveData<Array<String>>()

    val isAccessingDatabase = MutableLiveData<Boolean>()
    val canExit = MutableLiveData<Boolean>()


    init {
        //Utilizzo +1 perché in libreria Calendar i mesi partono da 0, mentre in Month partono da 1
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        newBookStartDate = StartDate(day, month, year)
    }

    //fun updateTitle() { currentTitle.value = newBookTitle }

    //fun updateAuthor() { currentAuthor.value = newBookAuthor }

    fun updateStartDate() {
        //Need +1 because January is mapped on 1 in the conversion, but the DatePicker let months starts with 0
        currentStartDateString.value = "${newBookStartDate.startDay} ${Month.of(newBookStartDate.startMonth).getDisplayName(TextStyle.FULL, Locale.getDefault())} ${newBookStartDate.startYear}"
    }

    fun updateLink() { currentLink.value = newBookCoverLink }

    //fun updateSupport() { currentSupport.value = newBookSupport }

    fun updatePages() {
        currentPages.value = newBookPages
    }



    fun updateAuthorList() {
        var authorArray : Array<String>
        isAccessingDatabase.value = true
        viewModelScope.launch {
            authorArray = newReadingBookModel.getAuthorList()
            currentAuthorArray.value = authorArray
            isAccessingDatabase.value = false
        }
    }


    fun addNewBook(title: String, author: String) {
        isAccessingDatabase.value = true
        Log.i("Nicosanti", "Aggiungo Libro")
        CoroutineScope(Dispatchers.Main).launch {
            newReadingBookModel.addBookToDatabase(title,
                    author,
                    newBookStartDate,
                    newBookCoverLink,
                    newBookSupport,
                    newBookPages,
                    readState)
            Log.i("SIMONE_NICOSANTI" , "NewReadingVM : Libro aggiunto")
            Log.i("Nicosanti", "Aggiunto")
            isAccessingDatabase.value = false
            canExit.value = true
            Log.i("Nicosanti", "Aggiunto")
        }
    }
}