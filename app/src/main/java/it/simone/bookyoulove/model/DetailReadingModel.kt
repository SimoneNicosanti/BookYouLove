package it.simone.bookyoulove.model

import android.content.Context
import android.util.Log
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DetailReadingModel(private val myAppDatabase: AppDatabase) {

    suspend fun loadDetailReadingBookFromDatabase(detailTitle: String, detailAuthor: String, detailTime: Int): Book? {
        val readingBook : Book
        withContext(Dispatchers.IO) {
            readingBook = myAppDatabase.bookDao().loadSpecificBook(detailTitle, detailAuthor, detailTime)
        }
        return readingBook
    }

}

