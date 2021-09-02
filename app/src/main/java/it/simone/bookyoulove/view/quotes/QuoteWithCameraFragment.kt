package it.simone.bookyoulove.view.quotes


import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import com.yalantis.ucrop.UCrop
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentQuoteWithCameraBinding
import it.simone.bookyoulove.view.dialog.AlertDialogFragment
import it.simone.bookyoulove.view.setViewEnable
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.Executors


@androidx.camera.lifecycle.ExperimentalUseCaseGroupLifecycle
class QuoteWithCameraFragment : Fragment() {

    private lateinit var binding: FragmentQuoteWithCameraBinding

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private val quoteWithCameraVM: QuoteWithCameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        binding = FragmentQuoteWithCameraBinding.inflate(inflater, container, false)
        setViewEnable(true, requireActivity())

        return binding.root
    }


    private fun setObservers() {
        val canExitWithTextObservers = Observer<String?> {
            if (it != null) {
                findNavController().previousBackStackEntry?.savedStateHandle?.set("scannedQuoteKey", it)
                findNavController().popBackStack()
            }
            else {
                val alertDialog = AlertDialogFragment()
                val arguments = bundleOf("alertDialogTitleKey" to getString(R.string.scan_error_string))
                alertDialog.arguments = arguments
                alertDialog.show(childFragmentManager, "Scan Text Error")

            }
        }
        quoteWithCameraVM.canExitWithText.observe(viewLifecycleOwner, canExitWithTextObservers)

        val isAccessingObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                setViewEnable(false, requireActivity(), )
                binding.quoteWithCameraLoading.root.visibility = View.VISIBLE
            }

            else {
                setViewEnable(true, requireActivity(), )
                binding.quoteWithCameraLoading.root.visibility = View.GONE
            }
        }
        quoteWithCameraVM.isAccessing.observe(viewLifecycleOwner, isAccessingObserver)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
             bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))

    }


    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {

        val cameraSelector: CameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

        val preview: Preview = Preview.Builder()
                //.setTargetAspectRatio(AspectRatio.RATIO_4_3 )
                .build()

        val previewView = requireView().findViewById<PreviewView>(R.id.quoteWithCameraPreviewView)
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    //.setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .build()


        val viewPort = previewView.viewPort

        val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageCapture as UseCase)
                .setViewPort(viewPort!!)
                .build()

        cameraProvider?.bindToLifecycle(viewLifecycleOwner, cameraSelector, useCaseGroup)

        binding.quoteWithCameraTakePictureButton.setOnClickListener {

            val inputFile = File(requireContext().filesDir, "quoteWithCameraFileInput")
            val outputFile = File(requireContext().filesDir, "quoteWithCameraFileOutput")

            val quoteImageFile = ImageCapture.OutputFileOptions.Builder(inputFile).build()
            imageCapture.run {
                this.takePicture(
                        quoteImageFile,
                        //@CameraExecutor CameraXExecutors.ioExecutor(),
                        Executors.newSingleThreadExecutor(),
                        object : ImageCapture.OnImageSavedCallback {

                            @Suppress("DEPRECATION")
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                                val uCrop = UCrop.of(Uri.fromFile(inputFile),
                                        Uri.fromFile(outputFile))
                                startActivityForResult(uCrop.getIntent(requireContext()), UCrop.REQUEST_CROP)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                val alertDialog = AlertDialogFragment()
                                val args = bundleOf("alertDialogTitleKey" to getString(R.string.capture_error_string))
                                alertDialog.arguments = args
                                alertDialog.show(childFragmentManager, "Capture Image Error Dialog")
                            }

                        }
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP && data != null) {
            val resultUri : Uri = UCrop.getOutput(data)!!
            quoteWithCameraVM.scanText(resultUri)
        }
        else if (resultCode == UCrop.RESULT_ERROR) {
            val alertDialog = AlertDialogFragment()
            val args = bundleOf("alertDialogTitleKey" to getString(R.string.crop_error_string))
            alertDialog.arguments = args
            alertDialog.show(childFragmentManager, "Crop Image Error Dialog")
        }
    }

}


class QuoteWithCameraViewModel(application: Application) : AndroidViewModel(application) {

    val canExitWithText = MutableLiveData<String?>()
    val isAccessing = MutableLiveData<Boolean>()

    private val myApp = application

    fun scanText(fileUri : Uri) {
        isAccessing.value = true
        viewModelScope.launch {
            val image : InputImage
            withContext(Dispatchers.IO) {
                image = InputImage.fromFilePath(myApp.applicationContext, fileUri)
            }
            withContext(Dispatchers.Default) {
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        viewModelScope.launch {
                            var scannedQuote = ""
                            withContext(Dispatchers.Default) {
                                for (block in visionText.textBlocks) {
                                    for (line in block.lines) {
                                        for (element in line.elements) {
                                            scannedQuote = scannedQuote + " " + element.text
                                        }
                                    }
                                }
                            }
                            withContext(Dispatchers.Main) {
                                isAccessing.value = false
                                canExitWithText.value = scannedQuote
                            }
                        }
                    }

                    .addOnFailureListener {
                        isAccessing.value = false
                        canExitWithText.value = null
                    }
            }
        }
    }
}