package it.simone.bookyoulove.view

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.pm.ActivityInfo
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.camera.core.*
import androidx.camera.core.impl.utils.Exif
import androidx.camera.core.impl.utils.executor.CameraXExecutors
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import it.simone.bookyoulove.databinding.FragmentQuoteWithCameraBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class QuoteWithCameraFragment : Fragment() {

    private lateinit var binding : FragmentQuoteWithCameraBinding

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentQuoteWithCameraBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        } , ContextCompat.getMainExecutor(requireContext()))

    }



    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {
        val preview : Preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()

        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val previewView = binding.quoteWithCameraPreviewView as PreviewView
        preview.setSurfaceProvider(previewView.surfaceProvider)


        val imageAnalysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()


        val imageCapture = ImageCapture.Builder()
                .setTargetRotation(previewView.display.rotation)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()

        binding.quoteWithCameraTakePictureButton.setOnClickListener {
            val quoteImageFile = ImageCapture.OutputFileOptions.Builder(File(requireContext().filesDir, "quoteWithCameraFile")).build()
            imageCapture.takePicture(
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
        
        cameraProvider?.bindToLifecycle(viewLifecycleOwner, cameraSelector, imageCapture, imageAnalysis, preview)

    }


}