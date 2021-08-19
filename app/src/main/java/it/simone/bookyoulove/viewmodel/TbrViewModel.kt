package it.simone.bookyoulove.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.model.TbrModel

class TbrViewModel(application: Application) : AndroidViewModel(application) {

    val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    val tbrModel = TbrModel(myAppDatabase)
}