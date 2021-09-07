package it.simone.bookyoulove.model

import android.app.Application
import android.webkit.MimeTypeMap
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.gson.Gson
import it.simone.bookyoulove.Constants.GOOGLE_DRIVE_BACKUP_NAME
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.BookSupport
import it.simone.bookyoulove.database.entity.Quote
import it.simone.bookyoulove.database.entity.Rate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileWriter

const val GOOGLE_DRIVE_BACKUP_FOLDER = "appDataFolder"
//https://www.section.io/engineering-education/backup-services-with-google-drive-api-in-android/

class GoogleDriveModel(private val driveService : Drive, private val myApp : Application) {

    suspend fun uploadBackupOnGoogleDrive() {
        withContext(Dispatchers.IO) {
            //Creo file Json con all'interno tutti i dati del DB più un intero che indica la versione del Backup
            val jsonBookArray = createJsonBookArray()
            val jsonQuoteArray = createJsonQuoteArray()

            val jsonBackupObject = JSONObject()
            jsonBackupObject.put("Version", 1)
            jsonBackupObject.put("Book", jsonBookArray)
            jsonBackupObject.put("Quote", jsonQuoteArray)

            val backupFile = File(myApp.applicationContext.filesDir, GOOGLE_DRIVE_BACKUP_NAME)
            val fileWriter = FileWriter(backupFile)
            fileWriter.write(jsonBackupObject.toString())
            fileWriter.flush()
            fileWriter.close()

            val backupFileContent = FileContent(
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(backupFile.extension),
                backupFile)

            //Verifico esistenza di Backup precedente
            val backupExistsId = verifyBackupPresent(driveService)


            //Costruisco il nuovo file di Backup per google drive
            val driveBackup = com.google.api.services.drive.model.File()
            driveBackup.parents = listOf(GOOGLE_DRIVE_BACKUP_FOLDER)
            driveBackup.name = GOOGLE_DRIVE_BACKUP_NAME


            //Se esiste precedente lo cancello e poi carico il nuovo
            if (backupExistsId != "") {
                driveService.Files().delete(backupExistsId).execute()
            }
            driveService.Files().create(driveBackup, backupFileContent).execute()

            //Cancello il file di backup dalla memoria locale per evitare sprechi di memoria
            backupFile.delete()
        }
    }

    private fun createJsonQuoteArray(): JSONArray {
        val quoteArray = AppDatabase.getDatabaseInstance(myApp.applicationContext).quoteDao().loadAllQuotes()

        val jsonQuoteArray = JSONArray()
        for (quote in quoteArray) {
            val jsonQuote = Gson().toJson(BackupQuote().fromQuote(quote))
            jsonQuoteArray.put(JSONObject(jsonQuote))
        }
        return jsonQuoteArray
    }

    private fun createJsonBookArray(): JSONArray {
        val bookArray = AppDatabase.getDatabaseInstance(myApp.applicationContext).bookDao().loadAllBooks()

        val jsonBookArray = JSONArray()
        for (book in bookArray) {
            val jsonBook = Gson().toJson(BackupBook().fromBook(book))
            jsonBookArray.put(JSONObject(jsonBook))
        }

        return jsonBookArray
    }


    suspend fun downloadBackupFromGoogleDrive() {
        withContext(Dispatchers.IO) {
            val backupExistsId = verifyBackupPresent(driveService)
            if (backupExistsId != "") {
                //Se il backup esiste ne scarico il contenuto
                val downloadedStream = ByteArrayOutputStream()
                driveService.Files().get(backupExistsId)
                    .setAlt("media")
                    .executeMediaAndDownloadTo(downloadedStream)
                //Il contenuto viene emesso nel file in cui è scritto il database

                val jsonBackupObject = JSONObject(downloadedStream.toString())

                downloadedStream.close()

                restoreBookInfo(jsonBackupObject.getJSONArray("Book"))
                restoreQuoteInfo(jsonBackupObject.getJSONArray("Quote"))
            }
        }
    }

    private fun restoreBookInfo(jsonBookArray: JSONArray) {
        val appDatabase = AppDatabase.getDatabaseInstance(myApp.applicationContext)
        for (index in 0 until jsonBookArray.length()) {
            val backupBook = Gson().fromJson(jsonBookArray.getJSONObject(index).toString(), BackupBook::class.java)
            val restoredBook = backupBook.toBook()
            appDatabase.bookDao().insertBooks(restoredBook)
        }
    }

    private fun restoreQuoteInfo(jsonQuoteArray: JSONArray) {
        val addDatabase = AppDatabase.getDatabaseInstance(myApp.applicationContext)
        for (index in 0 until jsonQuoteArray.length()) {
            val backupQuote = Gson().fromJson(jsonQuoteArray.getJSONObject(index).toString(), BackupQuote::class.java)
            val restoredQuote = backupQuote.toQuote()
            addDatabase.quoteDao().insertQuote(restoredQuote)
        }
    }


    private fun verifyBackupPresent(driveService : Drive) : String {
        val fileList = driveService.Files().list()
            .setSpaces(GOOGLE_DRIVE_BACKUP_FOLDER)
            .execute()
        for (file in fileList.files) {
            if (file.name == GOOGLE_DRIVE_BACKUP_NAME) return file.id
        }
        return ""
    }

    //è necessario utilizzare queste classi di supporto perché Json non supporta completamente i Long
    data class BackupBook (
            var bookIdString : String = "",
            var title : String = "",
            var author : String = "",
            var startDateString: String? = null,
            var endDateString: String? = null,
            var support : BookSupport? = null,
            var coverName: String = "",
            var pages: Int? = null,
            var rate: Rate? = null,
            var finalThought : String = "",
            var readState: Int = 0
    ) {
        fun fromBook(book : Book) : BackupBook {
            bookIdString = book.bookId.toString()
            title = book.title
            author = book.author
            startDateString = (if (book.startDate != null) book.startDate.toString() else null)
            endDateString = (if  (book.endDate != null) book.endDate.toString() else null)
            support = book.support
            coverName = book.coverName
            pages = book.pages
            rate = book.rate
            finalThought = book.finalThought
            readState = book.readState

            return this
        }

        fun toBook() : Book {
            return Book(
                    bookIdString.toLong(),
                    title,
                    author,
                    if (startDateString != null) startDateString!!.toLong() else null,
                    if (endDateString != null) endDateString!!.toLong() else null,
                    support,
                    coverName,
                    pages,
                    rate,
                    finalThought,
                    readState)
        }
    }

    data class BackupQuote (
            var quoteIdString : String = "",
            var bookIdString : String = "",
            var quoteText : String = "",
            var bookTitle : String = "",
            var bookAuthor : String = "",
            var favourite : Boolean = false,
            var toWidget : Boolean = false,
            var quotePage : Int = 0,
            var quoteChapter : String = "",
            var quoteThought : String = "",
            var dateString : String = ""
    ) {
        fun fromQuote(quote : Quote) : BackupQuote {
            quoteIdString = quote.quoteId.toString()
            bookIdString = quote.bookId.toString()
            quoteText = quote.quoteText
            bookTitle = quote.bookTitle
            bookAuthor = quote.bookAuthor
            favourite = quote.favourite
            toWidget = quote.toWidget
            quoteChapter = quote.quoteChapter
            quoteThought = quote.quoteThought
            dateString = quote.date.toString()

            return this
        }

        fun toQuote() : Quote {
            return Quote(
                    quoteIdString.toLong(),
                    bookIdString.toLong(),
                    quoteText,
                    bookTitle,
                    bookAuthor,
                    favourite,
                    toWidget,
                    quotePage,
                    quoteChapter,
                    quoteThought,
                    dateString.toLong())
        }
    }

}