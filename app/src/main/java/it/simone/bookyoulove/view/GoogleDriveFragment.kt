package it.simone.bookyoulove.view

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentGoogleDriveBinding
import it.simone.bookyoulove.view.dialog.AlertDialogFragment
import it.simone.bookyoulove.viewmodel.GoogleDriveViewModel

const val GOOGLE_DRIVE_SIGN = 250

class GoogleDriveFragment : Fragment(), View.OnClickListener {

    private lateinit var binding : FragmentGoogleDriveBinding

    private val googleDriveVM : GoogleDriveViewModel by viewModels()


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        binding = FragmentGoogleDriveBinding.inflate(inflater, container, false)

        binding.let {
            it.googleDriveLoginButton.setOnClickListener(this)
            it.googleDriveLogoutButton.setOnClickListener(this)
            it.googleDriveUploadButton.setOnClickListener(this)
            it.googleDriveDownloadButton.setOnClickListener(this)
        }

        googleDriveVM.getUser()
        setObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setObservers() {
        val currentUserObserver = Observer<GoogleSignInAccount?> {
            binding.googleDriveUserName.text = if (it != null) it.account?.name else getString(R.string.no_user_found)

            binding.run {
                googleDriveLogoutButton.isEnabled = (it != null)
                googleDriveUploadButton.isEnabled = (it != null)
                googleDriveDownloadButton.isEnabled = (it != null)
                googleDriveLoginButton.isEnabled = (it == null)
            }

        }
        googleDriveVM.currentUser.observe(viewLifecycleOwner, currentUserObserver)

        val isAccessingObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.googleDriveLoading.root.visibility = View.VISIBLE
            }

            else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.googleDriveLoading.root.visibility = View.GONE
            }
        }
        googleDriveVM.isAccessing.observe(viewLifecycleOwner, isAccessingObserver)

        val operationCompletedObserver = Observer<String> { completeMessage ->
            if (completeMessage != "") {
                val alertDialog = AlertDialogFragment()
                alertDialog.arguments = bundleOf("alertDialogTitleKey" to completeMessage)
                alertDialog.show(childFragmentManager, "")
                googleDriveVM.resetMessage()
            }
        }
        googleDriveVM.operationCompleted.observe(viewLifecycleOwner, operationCompletedObserver)
    }

    override fun onClick(v: View?) {
        when (v) {

            binding.googleDriveLoginButton -> {
                val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
                        .build()
                val signInClient = GoogleSignIn.getClient(requireContext(), signInOptions)

                startActivityForResult(signInClient.signInIntent, GOOGLE_DRIVE_SIGN)
            }

            binding.googleDriveLogoutButton -> {
                val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
                        .build()
                val signInClient = GoogleSignIn.getClient(requireContext(), signInOptions)
                signInClient.signOut().addOnSuccessListener {
                    googleDriveVM.getUser()
                }

            }

            binding.googleDriveUploadButton -> {
                googleDriveVM.uploadBackup()
            }

            binding.googleDriveDownloadButton -> {
                googleDriveVM.downloadBackup()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == RESULT_OK && requestCode == GOOGLE_DRIVE_SIGN) {
            googleDriveVM.getUser()
        }
        else {
            val alertDialog = AlertDialogFragment()
            val args = bundleOf("alertDialogTitleKey" to getString(R.string.signin_error_string))
            alertDialog.arguments = args
            alertDialog.show(childFragmentManager, "Error Sign in Fragment")
        }
    }

}