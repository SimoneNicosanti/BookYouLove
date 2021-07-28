package it.simone.bookyoulove.model

import android.content.Context
import android.widget.Toast
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.BookSupport
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.view.AUDIOBOOK_SUPPORT
import it.simone.bookyoulove.view.EBOOK_SUPPORT
import it.simone.bookyoulove.view.PAPER_SUPPORT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewReadingBookModel(val context: Context){

    val appDatabase = AppDatabase.getDatabaseInstance(context)


    suspend fun addBookToDatabase(
        title: String,
        author: String,
        startDate: StartDate,
        coverLink: String,
        support: MutableMap<String, Boolean>,
        bookPages: Int,
        readState: Int
    ) {
        //Mi permette di verificare se vengono fatte copie duplicate dello stesso libro nello stesso stato: Importante per Reading e TBR, mentre per Read è concesso
        val presented : Boolean = checkPresenceByState(title, author, readState)
        //Trova modo per non riptere due volte l'operazione di prelievo di same book
        if (!presented) {
            val computedSupport = computeSupport(support)
            val readTime = computeReadTime(title, author, appDatabase)

            val newBook = Book(title,
                    author,
                    readTime,
                    startDate,
                    null,
                    computedSupport,
                    coverLink,
                    bookPages,
                    null,
                    null,
                    readState
            )

            withContext(Dispatchers.IO) {
                appDatabase.bookDao().insertBooks(newBook)
            }
        }
    }

    private suspend fun checkPresenceByState(title: String, author: String, readState: Int): Boolean {
        val sameBookArray: Array<Book>
        withContext(Dispatchers.IO) {
            sameBookArray = appDatabase.bookDao().loadSameBook(title, author)
            //withContext(Dispatchers.Default) {
                //}
            }
        //LAVORO POSSIBILMENTE PESANTE FATTO NEL MAIN THREAD
        for (book in sameBookArray) {
            if (book.readState == readState) return true
        }
        //TODO("Trova un modo per ritornare valori dall'interno di un withContext")
        return false
    }



    private fun computeSupport(supportMap: MutableMap<String, Boolean>) : BookSupport {
        return BookSupport(supportMap[PAPER_SUPPORT]!!, supportMap[EBOOK_SUPPORT]!!, supportMap[AUDIOBOOK_SUPPORT]!!)
    }


    private suspend fun computeReadTime(title: String, author: String, appDatabase: AppDatabase) : Int {
        val sameBookList: Array<Book>
        var maxReadTime = 0
        withContext(Dispatchers.IO) {
            sameBookList = appDatabase.bookDao().loadSameBook(title, author)
            val readTimeArray: Array<Int>
            for (book in sameBookList) {
                if (book.readTime > maxReadTime) maxReadTime = book.readTime
            }
        }
        return maxReadTime + 1
    }


    private fun capitalizeAll(string: String) : String {
        return string.split(" ").joinToString(" ") { it.toLowerCase().capitalize() }
    }


    suspend fun getAuthorList() : Array<String> {
        val authorArray : Array<String>
        withContext(Dispatchers.IO) {
            authorArray = AppDatabase.getDatabaseInstance(context).bookDao().loadAuthorsList()
        }
        val filteredAuthorList : Array<String>
        withContext(Dispatchers.Default) {
            filteredAuthorList = authorArray.distinct().toTypedArray()
        }
        Toast.makeText(context, "Coroutine", Toast.LENGTH_SHORT).show()
        return filteredAuthorList
    }

}