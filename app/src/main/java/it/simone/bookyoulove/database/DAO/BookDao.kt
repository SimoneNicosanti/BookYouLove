package it.simone.bookyoulove.database.DAO

import androidx.room.*
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.EndDate
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.model.ChartsBookData
import java.util.ArrayList


data class NotFormattedChartsBookData(
        @ColumnInfo(name = "title") var title : String,
        @ColumnInfo(name = "author") var author : String,
        @ColumnInfo(name = "readTime") var readTime : Int,
        @ColumnInfo(name = "startDay") var startDay : Int,
        @ColumnInfo(name = "startMonth") var startMonth : Int,
        @ColumnInfo(name = "startYear") var startYear : Int,
        @ColumnInfo(name = "endDay") var endDay : Int,
        @ColumnInfo(name = "endMonth") var endMonth : Int,
        @ColumnInfo(name = "endYear") var endYear : Int,
        @ColumnInfo(name = "paperSupport") var paperSupport: Boolean,
        @ColumnInfo(name = "ebookSupport") var ebookSupport: Boolean,
        @ColumnInfo(name = "audiobookSupport") var audiobookSupport: Boolean,
        @ColumnInfo(name = "pages") var pages : Int,
        @ColumnInfo(name = "totalRate") var totalRate : Float,
        @ColumnInfo(name = "styleRate") var styleRate : Float,
        @ColumnInfo(name = "emotionRate") var emotionRate : Float,
        @ColumnInfo(name = "plotRate") var plotRate : Float,
        @ColumnInfo(name = "characterRate") var characterRate : Float
)


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

    @Query("SELECT * FROM Book WHERE totalRate > :requestedRate")
    fun loadBookByRate(requestedRate: Float) : Array<Book>

    @Query("SELECT author FROM Book")
    fun loadAuthorsList(): Array<String>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM Book WHERE readState LIKE :requestedState")
    fun loadShowedBookInfoByState(requestedState: Int) : Array<NotFormattedShowedBookInfo>

    @Query("SELECT * FROM Book WHERE title LIKE :requestedTitle AND author LIKE :requestedAuthor AND readTime LIKE :requestedTime")
    fun loadSpecificBook(requestedTitle: String, requestedAuthor: String, requestedTime: Int) : Book


    @Query("UPDATE Book SET finalThought = :newFinalThought WHERE title LIKE :requestedTitle AND author LIKE :requestedAuthor AND readTime LIKE :requestedTime")
    fun updateFinalThoughtByKey(requestedTitle: String, requestedAuthor: String, requestedTime: Int, newFinalThought : String)


    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM Book WHERE readState LIKE :requestedState")
    fun loadChartsData(requestedState : Int) : Array<ChartsBookData>
}