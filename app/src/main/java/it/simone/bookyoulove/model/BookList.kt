package it.simone.bookyoulove.model



import it.simone.bookyoulove.Constants.SORT_BY_AUTHOR
import it.simone.bookyoulove.Constants.SORT_BY_TITLE
import it.simone.bookyoulove.Constants.SORT_END_DATE
import it.simone.bookyoulove.Constants.SORT_START_DATE
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class BookList(private var loadedArray : MutableList<ShowedBookInfo>) {

    private var selectedItem : ShowedBookInfo? = null

    private var currentSortType = "start_date"

    operator fun get(position : Int): ShowedBookInfo {
        return this.loadedArray[position]
    }

    fun getValue(): MutableList<ShowedBookInfo> {
        return this.loadedArray
    }

    fun setItem(newSelectedItem : ShowedBookInfo) {
        selectedItem = newSelectedItem
    }


    suspend fun onArrayItemChanged(modifiedBook : Book) {
        withContext(Dispatchers.Default) {
            if (selectedItem != null) {
                val modifiedShowBookInfo = ShowedBookInfo(
                        modifiedBook.bookId,
                        modifiedBook.title,
                        modifiedBook.author,
                        modifiedBook.coverName,
                        modifiedBook.startDate,
                        modifiedBook.endDate,
                        modifiedBook.rate?.totalRate,
                        modifiedBook.pages
                )

                val originalPosition = findOriginalPosition()
                this@BookList.loadedArray[originalPosition] = modifiedShowBookInfo
                sortBookList(currentSortType)
                selectedItem = null
                //filterArray(searchField, searchType)
            }
        }
    }


    suspend fun onArrayItemRemoved() {
        //L'unico che può essere eliminato è il corrente
        withContext(Dispatchers.Default) {
            if (selectedItem != null) {
                val originalPosition = findOriginalPosition()
                this@BookList.loadedArray.removeAt(originalPosition)
                //filterArray(searchField, searchType)
                selectedItem = null
            }
        }
    }

    suspend fun onNewBookAdded(newBook: Book?) {
        withContext(Dispatchers.Default) {
            if (newBook != null) {
                val newShownBookInfo = ShowedBookInfo(
                        bookId = newBook.bookId,
                        title = newBook.title,
                        author = newBook.author,
                        coverName = newBook.coverName,
                        startDate = newBook.startDate,
                        endDate = newBook.endDate,
                        totalRate = newBook.rate?.totalRate,
                        pages = newBook.pages
                )
                loadedArray.add(newShownBookInfo)
                sortBookList(currentSortType)
            }
        }
    }


    private fun findOriginalPosition(): Int {
        return this.loadedArray.indexOf(selectedItem!!)
    }


    suspend fun sortBookList(sortType: String) {
        currentSortType = sortType
        withContext(Dispatchers.Default) {
            loadedArray = when (sortType) {
                "start_date" -> sortByDate(this@BookList.loadedArray, SORT_START_DATE)
                "end_date" -> sortByDate(this@BookList.loadedArray, SORT_END_DATE)
                "title" -> sortByTitleOrAuthor(this@BookList.loadedArray, SORT_BY_TITLE)
                "author" -> sortByTitleOrAuthor(this@BookList.loadedArray, SORT_BY_AUTHOR)
                "pages" -> loadedArray.sortedBy { it.pages }.toMutableList()

                else -> this@BookList.loadedArray
            }
        }
    }


    private suspend fun sortByDate(bookArray: MutableList<ShowedBookInfo>, sortType: Int): MutableList<ShowedBookInfo> {
        withContext(Dispatchers.Default) {

            if (sortType == SORT_START_DATE) bookArray.sortBy { it.startDate }
            else bookArray.sortBy { it.endDate }

            //I libri più vecchi sono quelli che stanno per primi --> Inverto l'array
            bookArray.reverse()
        }
        return bookArray
    }

    private suspend fun sortByTitleOrAuthor(notSortedArray: MutableList<ShowedBookInfo>, sortType: Int): MutableList<ShowedBookInfo> {
        withContext(Dispatchers.Default) {
            if (sortType == SORT_BY_TITLE) notSortedArray.sortBy{ it.title }
            else notSortedArray.sortBy { it.author }
        }
        return notSortedArray.toMutableList()
    }

    fun getSelectedItem(): ShowedBookInfo? {
        return selectedItem
    }
}