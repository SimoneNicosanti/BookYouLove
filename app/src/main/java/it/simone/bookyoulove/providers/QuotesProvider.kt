 package it.simone.bookyoulove.providers

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import it.simone.bookyoulove.database.AppDatabase
import java.lang.IllegalArgumentException


private const val AUTHORITY = "it.simone.bookyoulove.providers"
private const val TABLE_NAME = "QuotesTable"
private const val CODE_GET_RANDOM_QUOTE = 0

class QuotesProvider : ContentProvider() {

    private val matcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, "$TABLE_NAME/random", CODE_GET_RANDOM_QUOTE)
    }

    val URI_RANDOM: Uri = Uri.parse("content://$AUTHORITY/$TABLE_NAME/random")


    private lateinit var myAppDatabase : AppDatabase

    override fun onCreate(): Boolean {
        myAppDatabase = AppDatabase.getDatabaseInstance(context!!)
        return true
    }


    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {

        when (matcher.match(uri)) {
            CODE_GET_RANDOM_QUOTE -> {
                val randomQuoteCursor = myAppDatabase.quoteDao().loadRandomQuoteCursor()
                randomQuoteCursor.setNotificationUri(context?.contentResolver, uri)
                return randomQuoteCursor
            }

            else -> throw IllegalArgumentException("No URI found")
        }
    }

    override fun getType(uri: Uri): String {
        return "vnd.android.cursor.item/vnd.it.simone.bookyoulove.providers.QuotesProvider.$TABLE_NAME"
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }
}