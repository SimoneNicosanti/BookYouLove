package it.simone.bookyoulove.view.quotes


import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Rational
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import com.yalantis.ucrop.UCrop
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentQuoteWithCameraBinding
import kotlinx.coroutines.*
import java.io.File
import java.lang.Runnable
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

        return binding.root
    }


    private fun setObservers() {
        val canExitWithTextObservers = Observer<String?> {
            if (it != null) {
                findNavController().previousBackStackEntry?.savedStateHandle?.set("scannedQuoteKey", it)
                findNavController().popBackStack()
            }
            else {
                val errorSnackBar = Snackbar.make(requireView(), getString(R.string.scan_error_string), Snackbar.LENGTH_SHORT)
                errorSnackBar.anchorView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                errorSnackBar.show()
            }
        }
        quoteWithCameraVM.canExitWithText.observe(viewLifecycleOwner, canExitWithTextObservers)

        val isAccessingObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.quoteWithCameraLoading.root.visibility = View.VISIBLE
            }

            else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.quoteWithCameraLoading.root.visibility = View.GONE
            }
        }
        quoteWithCameraVM.isAccessing.observe(viewLifecycleOwner, isAccessingObserver)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        //Devo impostare la rotazione DOPO la creazione della view, altrimenti ho GIUSTAMENTE crash
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
                .setTargetAspectRatio(AspectRatio.RATIO_4_3 )
                .build()

        val previewView = requireView().findViewById<PreviewView>(R.id.quoteWithCameraPreviewView)
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
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
                                val captureSnackBar = Snackbar.make(requireView(), getString(R.string.capture_error_string), Snackbar.LENGTH_SHORT)
                                captureSnackBar.anchorView = requireActivity().findViewById(R.id.bottomNavigationView)
                                captureSnackBar.show()
                            }

                        }
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("Nicosanti", "Result")
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP && data != null) {
            val resultUri : Uri = UCrop.getOutput(data)!!
            quoteWithCameraVM.scanText(resultUri)
        }
        else if (resultCode == UCrop.RESULT_ERROR) {
            val cropSnackBar = Snackbar.make(requireView(), getString(R.string.crop_error_string), Snackbar.LENGTH_SHORT)
            cropSnackBar.anchorView = requireActivity().findViewById(R.id.bottomNavigationView)
            cropSnackBar.show()
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