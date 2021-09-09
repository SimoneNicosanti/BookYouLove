package it.simone.bookyoulove.view

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
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


class GoogleDriveFragment : Fragment(), View.OnClickListener {

    private lateinit var binding : FragmentGoogleDriveBinding

    private val googleDriveVM : GoogleDriveViewModel by viewModels()

    private val registerForSignInResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        onGoogleSignInActivity(result.resultCode, result.data)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        binding = FragmentGoogleDriveBinding.inflate(inflater, container, false)
        setViewEnable(true, requireActivity())

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
                setViewEnable(false, requireActivity())
                binding.googleDriveLoading.root.visibility = View.VISIBLE
            }

            else {
                setViewEnable(true, requireActivity())
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

                //@Suppress("DEPRECATION")
                //startActivityForResult(signInClient.signInIntent, GOOGLE_DRIVE_SIGN)
                registerForSignInResult.launch(signInClient.signInIntent)
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

    //Non è un override: la onActivityResult è deprecata!!
    private fun onGoogleSignInActivity(resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
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