package it.simone.bookyoulove.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import it.simone.bookyoulove.R
import it.simone.bookyoulove.model.GoogleDriveModel
import kotlinx.coroutines.launch


const val GOOGLE_DRIVE_BACKUP_NAME = "BookYouLoveDatabaseBackup"

class GoogleDriveViewModel(application: Application) : AndroidViewModel(application) {

    private var driveService : Drive? = null

    private val myApp = application

    val currentUser = MutableLiveData<GoogleSignInAccount?>()

    val isAccessing = MutableLiveData<Boolean>()
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
                driveService?.let { GoogleDriveModel(driveService!!, myApp).uploadBackupOnGoogleDrive() }
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


    fun downloadBackup() {
        isAccessing.value = true
        viewModelScope.launch {
            try {
                driveService?.let { GoogleDriveModel(driveService!!, myApp).downloadBackupFromGoogleDrive() }
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