package it.simone.bookyoulove.model

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ModifyEndedModel(private val myAppDatabase: AppDatabase) {

    suspend fun saveChangedBook(changedBook : Book) {
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().updateBooks(changedBook)
        }
    }
    
}