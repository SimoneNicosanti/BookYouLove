package it.simone.bookyoulove.view.quotes

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.ACTION_EDIT
import android.gesture.GestureLibraries.fromFile
import android.net.Uri
import android.net.Uri.fromFile
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.camera.core.impl.utils.Exif
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile.fromFile
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentQuoteWithCameraAnalyzerBinding
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.ModifyQuoteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException


class QuoteWithCameraAnalyzerFragment : Fragment() , View.OnClickListener{

    private lateinit var binding : FragmentQuoteWithCameraAnalyzerBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentQuoteWithCameraAnalyzerBinding.inflate(inflater, container, false)

        Picasso.get().load(File(requireContext().filesDir, "quoteWithCameraFile")).into(binding.quoteWithCameraAnalyzerImageView)

        binding.quoteWithCameraAnalyzerBackButton.setOnClickListener(this)
        binding.quoteWithCameraAnalyzerConfirmButton.setOnClickListener(this)
        binding.quoteWithCameraAnalyzerRotateButton.setOnClickListener(this)

        return binding.root
    }



    override fun onClick(v: View?) {

        when (v) {
            binding.quoteWithCameraAnalyzerBackButton -> requireActivity().onBackPressed()

            binding.quoteWithCameraAnalyzerConfirmButton -> {
                //Dovrebbero stare in VM e Model
                val loadingDialog = LoadingDialogFragment()
                loadingDialog.show(childFragmentManager, "Loading Fragment")
                CoroutineScope(Dispatchers.IO).launch {
                    var image: InputImage? = null
                    try {
                        image = InputImage.fromFilePath( requireContext(), Uri.fromFile(File(requireContext().filesDir, "quoteWithCameraFile")))
                    } catch (e : IOException) { e.printStackTrace() }

                    var scannedQuote : String? = null
                    withContext(Dispatchers.Default) {
                        if (image != null) {
                            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                            val resultQuote = recognizer.process(image)
                                .addOnSuccessListener { visionText ->
                                    scannedQuote = visionText.text
                                    loadingDialog.dismiss()

                                    //Non dovrei utilizzare un riferimento diretto al fragment a cui devo andare... però vabbè
                                    findNavController().getBackStackEntry(R.id.modifyQuoteFragment).savedStateHandle.set("scannedQuoteKey", scannedQuote)
                                    findNavController().popBackStack(R.id.modifyQuoteFragment, false)
                                }
                                .addOnFailureListener { e ->

                                }

                        }
                    }
                }
            }

            binding.quoteWithCameraAnalyzerRotateButton -> {

                CoroutineScope(Dispatchers.Default).launch {
                    val exif = Exif.createFromFile(File(requireContext().filesDir, "quoteWithCameraFile"))
                    //val rotation = exif.rotation
                    exif.rotate(90)
                    exif.save()
                    withContext(Dispatchers.Main) {
                        Picasso.get().load(File(requireContext().filesDir, "quoteWithCameraFile")).into(binding.quoteWithCameraAnalyzerImageView)
                    }
                }
            }
        }

    }
}
