package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import it.simone.bookyoulove.database.AppDatabase

class DetailEndedViewModel(application: Application) : AndroidViewModel(application) {

    private val myApp = application
    private val myAppDatabase = AppDatabase.getDatabaseInstance(myApp.applicationContext)


}