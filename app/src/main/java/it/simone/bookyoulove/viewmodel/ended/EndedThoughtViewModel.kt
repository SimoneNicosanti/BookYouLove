 package it.simone.bookyoulove.viewmodel.ended

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import it.simone.bookyoulove.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EndedThoughtViewModel(application: Application) : AndroidViewModel(application) {

    var currentThought = MutableLiveData("")
    val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)


    fun saveNewThought(endedBookId: Long) {
        //Dovrebbe stare in Model con una chiamata
        CoroutineScope(Dispatchers.IO).launch {
            myAppDatabase.bookDao().updateFinalThoughtByKey(endedBookId, currentThought.value!!)
        }
    }

    fun updateThought(text: String) {
        currentThought.value = text
    }

}