package it.simone.bookyoulove.model.reading

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DetailReadingModel(private val myAppDatabase: AppDatabase) {

    suspend fun loadDetailReadingBookFromDatabase(detailKeyTitle: Long): Book {
        val readingBook : Book
        withContext(Dispatchers.IO) {
            readingBook = myAppDatabase.bookDao().loadBookById(detailKeyTitle)
        }
        return readingBook
    }

}

