package it.simone.bookyoulove.viewmodel

import android.app.Application
import android.content.Context
import android.webkit.MimeTypeMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.gson.*
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.lang.Exception

const val GOOGLE_DRIVE_BACKUP_NAME = "BookYouLoveDatabaseBackup"
const val GOOGLE_DRIVE_BACKUP_FOLDER = "appDataFolder"


class GoogleDriveViewModel(application: Application) : AndroidViewModel(application) {

    private var driveService : Drive? = null

    private val myApp = application

    val currentUser = MutableLiveData<GoogleSignInAccount?>()

    val isAccessing = MutableLiveData(false)
    val operationCompleted = MutableLiveData("")

    fun getUser() {
        val lastAccount = GoogleSignIn.getLastSignedInAccount(myApp.applicationContext)
        currentUser.value = lastAccount

        if (lastAccount != null) {
            driveService = DriveStart(lastAccount, myApp.applicationContext).getDriveService()
        }
    }

    fun uploadBackup() {
        isAccessing.value = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    //Creo file Json con all'interno tutti i dati del DB
                    val jsonBookArray = createJsonBookArray()
                    val jsonQuoteArray = createJsonQuoteArray()

                    val jsonBackupObject = JSONObject()
                    jsonBackupObject.put("Book", jsonBookArray)
                    jsonBackupObject.put("Quote", jsonQuoteArray)

                    val backupFile = java.io.File(myApp.applicationContext.filesDir, GOOGLE_DRIVE_BACKUP_NAME)
                    val fileWriter = FileWriter(backupFile)
                    fileWriter.write(jsonBackupObject.toString())
                    fileWriter.flush()
                    fileWriter.close()

                    val backupFileContent = FileContent(
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension(backupFile.extension),
                            backupFile)

                    //Verifico esistenza di Backup precedente
                    val backupExistsId = verifyBackupPresent(driveService!!)


                    //Costruisco il nuovo file di Backup per google drive
                    val driveBackup = File()
                    driveBackup.parents = listOf(GOOGLE_DRIVE_BACKUP_FOLDER)
                    driveBackup.name = GOOGLE_DRIVE_BACKUP_NAME


                    //Se esiste precedente lo cancello e poi carico il nuovo
                    if (backupExistsId != "") {
                        driveService!!.Files().delete(backupExistsId).execute()
                    }
                    driveService!!.Files().create(driveBackup, backupFileContent).execute()

                    //Cancello il file di backup dalla memoria locale per evitare sprechi di memoria
                    backupFile.delete()
                }
                isAccessing.value = false
                operationCompleted.value = myApp.resources.getString(R.string.upload_completed_string)
            }
            catch (exception : Exception) {
                isAccessing.value = false
                operationCompleted.value = myApp.resources.getString(R.string.operation_wrong_string)
                exception.printStackTrace()
            }
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


    fun downloadBackup() {
        isAccessing.value = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val backupExistsId = verifyBackupPresent(driveService!!)
                    if (backupExistsId != "") {
                        //Se il backup esiste ne scarico il contenuto
                        val downloadedStream = ByteArrayOutputStream()
                        driveService!!.Files().get(backupExistsId)
                                .setAlt("media")
                                .executeMediaAndDownloadTo(downloadedStream)
                        //Il contenuto viene emesso nel file in cui è scritto il database
                        //downloadedStream.flush()


                        val jsonBackupObject = JSONObject(downloadedStream.toString())

                        downloadedStream.close()

                        restoreBookInfo(jsonBackupObject.getJSONArray("Book"))
                        restoreQuoteInfo(jsonBackupObject.getJSONArray("Quote"))
                    }
                }
                isAccessing.value = false
                operationCompleted.value = myApp.resources.getString(R.string.download_completed_string)
            }
            catch (exception : Exception) {
                isAccessing.value = false
                operationCompleted.value = myApp.resources.getString(R.string.operation_wrong_string)
                exception.printStackTrace()
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

    fun resetMessage() {
        operationCompleted.value = ""
    }
}



//https://www.section.io/engineering-education/backup-services-with-google-drive-api-in-android/
class DriveStart(private val signedInAccount: GoogleSignInAccount, private val context: Context) {

    private val APPLICATION_NAME = "BookYouLove"
    private val JSON_FACTORY = JacksonFactory.getDefaultInstance()

    private val SCOPES = listOf(DriveScopes.DRIVE_APPDATA)
    private val HTTP_TRANSPORT = NetHttpTransport()


    private fun getCredentials() : GoogleAccountCredential? {

        val credential = GoogleAccountCredential.usingOAuth2(context, SCOPES)
        credential.selectedAccount = signedInAccount.account!!

        return credential
    }


    fun getDriveService(): Drive? {

        val credential = getCredentials()
        return if (credential != null) {
            Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build()
        }
        else null
    }
}