package it.simone.bookyoulove.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UpdatedDatabaseViewModel: ViewModel() {

    var updatedReadingList = MutableLiveData<Boolean>()
    var updatedEndedList = MutableLiveData<Boolean>()
    var updatedTbrList = MutableLiveData<Boolean>()


    fun readingUpdated(updated: Boolean) {
        Log.i("SIMONE_NICOSANTI", "UpdatedDatabaseViewModel : Reading Update $updated")
        updatedReadingList.value = updated
    }

    fun tbrUpdated(updated : Boolean) {
        updatedTbrList.value = updated
    }

}