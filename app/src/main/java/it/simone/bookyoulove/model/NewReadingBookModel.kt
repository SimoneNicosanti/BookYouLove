package it.simone.bookyoulove.model

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.database.entity.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewReadingBookModel(val myAppDatabase : AppDatabase){
    //TODO("Potrei aggiungere richiesta di importare le citazioni vecchie se libro già letto")

    private suspend fun checkPresenceByState(keyTitle: String, keyAuthor: String, readState: Int): Boolean {
        val sameBookArray: Array<ShowedBookInfo>
        var isPresent = false
        withContext(Dispatchers.IO) {
            sameBookArray = myAppDatabase.bookDao().loadShowedBookInfoByState(readState)
        }
        //Me li vedo tutti ma non dovrebbero essere molti
        withContext(Dispatchers.Default) {
            for (book in sameBookArray) {
                if (book.keyTitle == keyTitle && book.keyAuthor == keyAuthor) isPresent = true
            }
        }
        return isPresent
        //TODO("Trova un modo per ritornare valori dall'interno di un withContext")
    }


    private suspend fun computeReadTime(keyTitle: String, keyAuthor: String) : Int {
        val sameBookArray: Array<Book>
        var maxReadTime = 0
        withContext(Dispatchers.IO) {
            sameBookArray = myAppDatabase.bookDao().loadSameBook(keyTitle, keyAuthor)
            withContext(Dispatchers.Default) {
                for (book in sameBookArray) {
                    if (book.readTime > maxReadTime) maxReadTime = book.readTime
                }
            }
        }
        return maxReadTime + 1
    }


    suspend fun loadAuthorArrayFromDatabase() : Array<String> {
        val authorArray : Array<String>
        withContext(Dispatchers.IO) {
            authorArray = myAppDatabase.bookDao().loadAuthorsList()
        }
        val filteredAuthorList : Array<String>
        withContext(Dispatchers.Default) {
            filteredAuthorList = authorArray.distinct().toTypedArray()
        }
        return filteredAuthorList
    }


    suspend fun loadReadingBookToModifyFromDatabase(title: String, author: String, readingTime: Int): Book {
        val loadedBook : Book
        withContext(Dispatchers.IO) {
            loadedBook = myAppDatabase.bookDao().loadSpecificBook(title, author, readingTime)
        }
        return loadedBook
    }


    suspend fun removeBookFromDatabase(bookToRemove: Book) {
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().deleteBooks(bookToRemove)
        }
    }

    suspend fun addNewBookInDatabase(newReadingBook : Book) {
        newReadingBook.keyTitle = formatKeyInfo(newReadingBook.title)
        newReadingBook.keyAuthor = formatKeyInfo(newReadingBook.author)
        //if (!checkPresenceByState(newReadingBook.keyTitle, newReadingBook.keyAuthor, READING_BOOK_STATE)) {
        newReadingBook.readTime = computeReadTime(newReadingBook.keyTitle, newReadingBook.keyAuthor)
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().insertBooks(newReadingBook)

        }
    }

    fun formatKeyInfo(notFormattedKey: String): String {
        //Todo("Potrei ad esempio farla più complessa : togliere spazi e simboli superflui
        var formattedKey = ""
        for (char in notFormattedKey) {
            formattedKey += char.toLowerCase()
        }
        return formattedKey
    }

    suspend fun updateReadingBookInDatabase(bookToUpdate: Book) {
        withContext(Dispatchers.IO) {
            myAppDatabase.bookDao().updateBooks(bookToUpdate)
        }
    }

    suspend fun changeQuotesInfoInDatabase(changedBook : Book) {

        withContext(Dispatchers.IO) {
            val oldQuotesArray = myAppDatabase.quoteDao().loadQuotesByBook(changedBook.keyTitle, changedBook.keyAuthor, changedBook.readTime)

            val newKeyTitle = formatKeyInfo(changedBook.title)
            val newKeyAuthor = formatKeyInfo(changedBook.author)
            val newReadTime = computeReadTime(newKeyTitle, newKeyAuthor) - 1

            for (quote in oldQuotesArray) {
                myAppDatabase.quoteDao().deleteQuote(quote)
                quote.keyTitle = newKeyTitle
                quote.keyAuthor = newKeyAuthor
                quote.readTime = newReadTime

                quote.bookTitle = changedBook.title
                quote.bookAuthor = changedBook.author
                myAppDatabase.quoteDao().insertQuote(quote)
            }
        }
    }

}