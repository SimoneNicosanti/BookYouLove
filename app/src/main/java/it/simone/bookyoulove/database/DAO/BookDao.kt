package it.simone.bookyoulove.database.DAO

import androidx.room.*
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.model.ChartsBookData



@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBooks(vararg newBook: Book)

    @Delete
    fun deleteBooks(vararg book: Book) : Int

    @Update
    fun updateBooks(vararg updatedBook: Book)


    @Query("SELECT * FROM Book")
    fun loadAllBooks() : Array<Book>

    /*  This function looks for a Book with given title and author. It give back a list of Book and the length
        of this list says the next read time I should use for the next time
     */

    @Query("SELECT * FROM Book WHERE totalRate > :requestedRate")
    fun loadBookByRate(requestedRate: Float) : Array<Book>

    @Query("SELECT author FROM Book")
    fun loadAuthorsList(): Array<String>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM Book WHERE readState LIKE :requestedState")
    fun loadShowedBookInfoByState(requestedState: Int) : Array<ShowedBookInfo>

    @Query("SELECT * FROM Book WHERE bookId LIKE :requestedBookId")
    fun loadBookById(requestedBookId: Long) : Book


    @Query("UPDATE Book SET finalThought = :newFinalThought WHERE bookId LIKE :requestedBookId")
    fun updateFinalThoughtByKey(requestedBookId: Long, newFinalThought: String)

    @Query("DELETE FROM Book WHERE bookId LIKE :requestedBookId")
    fun deleteBookById(requestedBookId: Long)


    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM Book WHERE readState LIKE :requestedState")
    fun loadChartsData(requestedState : Int) : Array<ChartsBookData>
}