package it.simone.bookyoulove.view.quotes


import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.core.impl.utils.executor.CameraXExecutors
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import it.simone.bookyoulove.databinding.FragmentQuoteWithCameraBinding
import kotlinx.coroutines.*
import java.io.File
import java.lang.Runnable


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
        val canExitWithTextObservers = Observer<String> {
            findNavController().previousBackStackEntry?.savedStateHandle?.set("scannedQuoteKey", it)
            findNavController().popBackStack()
        }
        quoteWithCameraVM.canExitWithText.observe(viewLifecycleOwner, canExitWithTextObservers)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        //Devo impostare la rotazione DOPO la creazione della view, altrimenti ho GIUSTAMENTE crash
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            if (cameraProvider != null) bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))

    }


    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {
        val preview: Preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

        val previewView = binding.quoteWithCameraPreviewView as PreviewView
        preview.setSurfaceProvider(previewView.surfaceProvider)


        val imageCapture = previewView.display?.rotation?.let {
            ImageCapture.Builder()
                    .setTargetRotation(it)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .build()
        }

        binding.quoteWithCameraTakePictureButton.setOnClickListener {

            val quoteImageFile = ImageCapture.OutputFileOptions.Builder(File(requireContext().filesDir, "quoteWithCameraFile")).build()
            imageCapture.run {
                this?.takePicture(
                        quoteImageFile,
                        CameraXExecutors.ioExecutor(),
                        object : ImageCapture.OnImageSavedCallback {

                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    //Log.i("Nicosanti", "Terminato Navigate")
                                    findNavController().navigate(QuoteWithCameraFragmentDirections.actionQuoteWithCameraFragmentToQuoteWithCameraAnalyzerFragment())
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                            }

                        }
                )
            }

            cameraProvider?.bindToLifecycle(viewLifecycleOwner, cameraSelector, imageCapture, preview)

        }

    }
}

class QuoteWithCameraViewModel : ViewModel() {

    val canExitWithText = MutableLiveData<String>()

    fun setCanExit(exitText : String) {
        canExitWithText.value = exitText
    }
}