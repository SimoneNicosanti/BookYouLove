package it.simone.bookyoulove.view


import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import ir.androidexception.roomdatabasebackupandrestore.Backup
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*


const val GOOGLE_DRIVE_UPLOAD = 400
const val GOOGLE_DRIVE_DOWNLOAD = 450


class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_fragment, rootKey)

        findPreference<Preference>("googleDriveUpload")?.onPreferenceClickListener = this
        findPreference<Preference>("googleDriveDownload")?.onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {

        if (preference?.key == "googleDriveUpload") {
            //Snackbar.make(requireView(), "Coming Soon", Snackbar.LENGTH_SHORT).show()
            getGoogleAccount(GOOGLE_DRIVE_UPLOAD)
            return true
        }
        if (preference?.key == "googleDriveDownload") {
            //Snackbar.make(requireView(), "Coming Soon", Snackbar.LENGTH_SHORT).show()
            getGoogleAccount(GOOGLE_DRIVE_DOWNLOAD)
            return true
        }

        return false
    }

    private fun getGoogleAccount(requestedOperation: Int) {
        val lastAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (lastAccount == null) {
            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
                .build()
            val signInClient = GoogleSignIn.getClient(requireContext(), signInOptions)

            startActivityForResult(signInClient.signInIntent, requestedOperation)

        }
        else {
            Log.d("Nicosanti", "Già autenticato")
            handleSignedIn(lastAccount, requestedOperation)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == RESULT_OK) {
            val signedInAccount = GoogleSignIn.getSignedInAccountFromIntent(data).result
            handleSignedIn(signedInAccount, requestCode)
        }
        else {
            Log.d("Nicosanti", "Fallimento")
        }
    }

    private fun handleSignedIn(signedInAccount: GoogleSignInAccount?, requestedOperation: Int) {
        if (signedInAccount != null) {
            val driveService = DriveStart(signedInAccount, requireContext()).getDriveService()
            Log.d("Nicosanti","$driveService")

                driveService?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            if (requestedOperation == GOOGLE_DRIVE_UPLOAD) {
                                //val file = File(requireContext().filesDir, "downloadedBackup")
                                uploadDatabaseBackup(driveService)
                            }
                            else if (requestedOperation == GOOGLE_DRIVE_DOWNLOAD) {
                                downloadDatabaseBackup(driveService)
                            }

                        }

                        catch (exception : UserRecoverableAuthIOException) {
                            startActivityForResult(exception.intent, requestedOperation)
                        }
                    }
                }
            }

        }

    private fun downloadDatabaseBackup(driveService: Drive) {
        
    }

    private fun uploadDatabaseBackup(driveService: Drive) {
        Backup.Init()
                .database(AppDatabase.getDatabaseInstance(requireContext()))
                .path(requireContext().filesDir.toString())
                .fileName("BookYouLoveDatabaseBackup")
                .execute()

        val backupExists = verifyBackupPresent(driveService)

        val driveBackup = File()
        driveBackup.parents = listOf("appDataFolder")
        driveBackup.name = "BookYouLoveDatabaseBackup"

        val backupFile = File(requireContext().filesDir, "BookYouLoveDatabaseBackup")
        val backupFileContent = FileContent(
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(backupFile.extension),
                backupFile)

        if (backupExists != "") {
            driveService.Files().delete(backupExists).execute()
        }
        driveService.Files().create(driveBackup, backupFileContent).execute()
    }


    private fun verifyBackupPresent(driveService : Drive) : String {
        val fileList = driveService.Files().list()
                .setSpaces("appDataFolder")
                .execute()
        for (file in fileList.files) {
            if (file.name == "BookYouLoveDatabaseBackup") return file.id
        }
        return ""
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
