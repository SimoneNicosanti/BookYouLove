package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import it.simone.bookyoulove.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EndedThoughtViewModel(application: Application) : AndroidViewModel(application) {

    var currentThought: String = ""
    val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)


    fun saveNewThought(title : String , author: String, time: Int) {
        //Dovrebbe stare in Model con una chiamata
        CoroutineScope(Dispatchers.IO).launch {
            myAppDatabase.bookDao().updateFinalThoughtByKey(title, author, time, currentThought)
        }
    }

    fun updateThought(text: String) {
        currentThought = text
    }

}