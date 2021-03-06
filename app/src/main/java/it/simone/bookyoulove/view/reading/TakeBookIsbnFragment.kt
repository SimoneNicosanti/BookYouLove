package it.simone.bookyoulove.view.reading


import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentTakeBookIsbnBinding
import it.simone.bookyoulove.view.dialog.AlertDialogFragment
import java.util.concurrent.Executors


class TakeBookIsbnFragment : Fragment() {

    private lateinit var binding : FragmentTakeBookIsbnBinding

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private val takeBookIsbnVM : TakeBookIsbnViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentTakeBookIsbnBinding.inflate(inflater, container, false)
        //setViewEnable(true, requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObserver()

        cameraProviderFuture.addListener( {
            val cameraProvider = cameraProviderFuture.get()
            if (cameraProvider != null) bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun setObserver() {
        val canExitWithIsbnObserver = Observer<String?> { finalIsbn ->
            if (finalIsbn != "" && finalIsbn != null) {
                findNavController().previousBackStackEntry?.savedStateHandle?.set("scannedIsbnKey", finalIsbn)
                findNavController().popBackStack()
            }
            else if (finalIsbn == null){
                val alertDialog = AlertDialogFragment()
                val args = bundleOf("alertDialogTitleKey" to getString(R.string.scan_isdn_error_string))
                alertDialog.arguments = args
                alertDialog.show(childFragmentManager, "")
            }
        }
        takeBookIsbnVM.canExitWithIsbn.observe(viewLifecycleOwner, canExitWithIsbnObserver)
    }


    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview : Preview = Preview.Builder()
                .build()

        val cameraSelector : CameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

        val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), IsbnAnalyzer(takeBookIsbnVM))

        val previewView = view?.findViewById<PreviewView>(R.id.takeBookIsbnCameraPreviewView)
        preview.setSurfaceProvider(previewView?.surfaceProvider)

        cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, imageAnalysis, preview)
    }


    private class IsbnAnalyzer(private val takeBookIsbnVM: TakeBookIsbnViewModel) : ImageAnalysis.Analyzer {

        override fun analyze(imageProxy: ImageProxy) {
            @androidx.camera.core.ExperimentalGetImage
            val isbnMediaImage = imageProxy.image
            @androidx.camera.core.ExperimentalGetImage
            if (isbnMediaImage != null) {
                val isbnImage = InputImage.fromMediaImage(isbnMediaImage, imageProxy.imageInfo.rotationDegrees)

                val scanningOptions = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_EAN_13)
                        .build()
                val isbnScanner = BarcodeScanning.getClient(scanningOptions)
                isbnScanner.process(isbnImage)
                        .addOnSuccessListener { scannedIsbn ->
                            if (scannedIsbn.isNotEmpty()) {
                                Log.i("Nicosanti", scannedIsbn[0]?.displayValue.toString())
                                takeBookIsbnVM.setScannedIsbn(scannedIsbn[0]?.displayValue.toString())
                            }
                        }
                        .addOnFailureListener {
                            //takeBookIsbnVM.setScannedIsbn("")
                            takeBookIsbnVM.setScannedIsbn(null)
                        }
                        .addOnCompleteListener {
                            if (it.isSuccessful) imageProxy.close()
                        }
            }
        }

    }
}

class TakeBookIsbnViewModel : ViewModel() {

    val canExitWithIsbn = MutableLiveData<String?>()

    fun setScannedIsbn(finalIsbn : String?) {
        canExitWithIsbn.value = finalIsbn
    }
}