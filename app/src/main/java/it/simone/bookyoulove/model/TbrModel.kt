package it.simone.bookyoulove.model

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.view.TBR_BOOK_STATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TbrModel(private val myAppDatabase : AppDatabase) {

    suspend fun loadTbrArrayFromDatabase(): Array<ShowedBookInfo> {
        val loadedArray : Array<ShowedBookInfo>
        withContext(Dispatchers.IO) {
            loadedArray = myAppDatabase.bookDao().loadShowedBookInfoByState(TBR_BOOK_STATE)
        }
        return loadedArray
    }

    suspend fun deleteTbrBook(deleteTbrBookId : Long) {
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().deleteBookById(deleteTbrBookId)
        }
    }
}