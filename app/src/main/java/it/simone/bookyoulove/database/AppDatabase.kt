package it.simone.bookyoulove.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import it.simone.bookyoulove.database.DAO.BookDao
import it.simone.bookyoulove.database.DAO.QuoteDao
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.Quote

const val DATABASE_NAME = "BookYouLoveDatabase"

@Database(entities = [Book::class, Quote::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun quoteDao() : QuoteDao

    companion object {
        private var appDatabase : AppDatabase? = null

        fun getDatabaseInstance(context: Context): AppDatabase {
            if (appDatabase == null) {
                appDatabase = Room.databaseBuilder(context,
                        AppDatabase::class.java,
                        DATABASE_NAME).build()
            }
            return appDatabase as AppDatabase
        }
    }
}