package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.model.ChartsBookData
import it.simone.bookyoulove.model.ChartsModel
import kotlinx.coroutines.launch

class ChartsViewModel(application : Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val chartsModel = ChartsModel(myAppDatabase)

    var currentChartsDataArray = MutableLiveData<Array<ChartsBookData>>()
    val isAccessingDatabase = MutableLiveData<Boolean>()

    private var loadedOnce = false


    fun getAllChartsData() {
        if (!loadedOnce) {
            isAccessingDatabase.value = true
            viewModelScope.launch {
                currentChartsDataArray.value = chartsModel.getAllChartsDataFromDatabase()
                loadedOnce = true
                isAccessingDatabase.value = false
            }
        }
    }


    fun changeLoadedStatus() {
        loadedOnce = false
    }
}

