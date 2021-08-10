package it.simone.bookyoulove.view

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.impl.utils.Exif
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
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
    private val modifyQuoteVM : ModifyQuoteViewModel by activityViewModels()

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
                        Log.i("Nicosanti", "OCR")
                        if (image != null) {
                            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                            val resultQuote = recognizer.process(image)
                                .addOnSuccessListener { visionText ->
                                    scannedQuote = visionText.text
                                    loadingDialog.dismiss()
                                    modifyQuoteVM.changeQuoteText(if (scannedQuote != null) scannedQuote!! else "")
                                    //Non si dovrebbe usare, ma questo mi evita di ripassare indietro per il fragment della camera
                                    findNavController().popBackStack(R.id.modifyQuoteFragment, false)
                                }
                                .addOnFailureListener { e ->

                                }

                        }
                    }
                }
            }

            binding.quoteWithCameraAnalyzerRotateButton -> {
                val exif = Exif.createFromFile(File(requireContext().filesDir, "quoteWithCameraFile"))
                val rotation = exif.rotation
                if (rotation != 0) {
                    exif.rotate(rotation)
                    exif.save()
                }
                Picasso.get().load(File(requireContext().filesDir, "quoteWithCameraFile")).into(binding.quoteWithCameraAnalyzerImageView)
            }
        }

    }
}
