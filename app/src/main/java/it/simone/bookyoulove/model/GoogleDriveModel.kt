package it.simone.bookyoulove.model

import android.app.Application
import android.webkit.MimeTypeMap
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.gson.Gson
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.Quote
import it.simone.bookyoulove.viewmodel.GOOGLE_DRIVE_BACKUP_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileWriter

const val GOOGLE_DRIVE_BACKUP_FOLDER = "appDataFolder"

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
            val jsonQuote = Gson().toJson(quote)
            jsonQuoteArray.put(JSONObject(jsonQuote))
        }
        return jsonQuoteArray
    }

    private fun createJsonBookArray(): JSONArray {
        val bookArray = AppDatabase.getDatabaseInstance(myApp.applicationContext).bookDao().loadAllBooks()

        val jsonBookArray = JSONArray()
        for (book in bookArray) {
            val jsonBook = Gson().toJson(book)
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
            val restoredBook = Gson().fromJson(jsonBookArray.getJSONObject(index).toString(), Book::class.java)
            appDatabase.bookDao().insertBooks(restoredBook)
        }
    }

    private fun restoreQuoteInfo(jsonQuoteArray: JSONArray) {
        val addDatabase = AppDatabase.getDatabaseInstance(myApp.applicationContext)
        for (index in 0 until jsonQuoteArray.length()) {
            val restoredQuote = Gson().fromJson(jsonQuoteArray.getJSONObject(index).toString(), Quote::class.java)
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
}