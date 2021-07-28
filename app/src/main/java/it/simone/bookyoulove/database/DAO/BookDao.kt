package it.simone.bookyoulove.database.DAO

import androidx.room.*
import it.simone.bookyoulove.database.entity.Book


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
    @Query("SELECT * FROM Book WHERE title LIKE :requestedTitle AND author LIKE :requestedAuthor")
    fun loadSameBook(requestedTitle: String, requestedAuthor: String): Array<Book>

    @Query("SELECT * FROM Book WHERE rate > :requestedRate")
    fun loadBookByRate(requestedRate: Float) : Array<Book>

    @Query("SELECT author FROM Book")
    fun loadAuthorsList(): Array<String>

    @Query("SELECT * FROM Book WHERE readState LIKE :requestedState")
    fun loadBookArrayByState(requestedState: Int) : Array<Book>

    @Query("SELECT * FROM Book WHERE title LIKE :requestedTitle AND author LIKE :requestedAuthor AND readTime LIKE :requestedTime")
    fun loadSpecificBook(requestedTitle: String, requestedAuthor: String, requestedTime: Int) : Book

}