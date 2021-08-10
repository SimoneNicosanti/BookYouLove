package it.simone.bookyoulove.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.common.util.concurrent.ListenableFuture
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Quote
import it.simone.bookyoulove.databinding.FragmentModifyQuoteBinding
import it.simone.bookyoulove.viewmodel.ModifyQuoteViewModel


class ModifyQuoteFragment : Fragment(), View.OnClickListener {

    private lateinit var binding : FragmentModifyQuoteBinding
    private val modifyQuoteVM : ModifyQuoteViewModel by activityViewModels()

    private val args : ModifyQuoteFragmentArgs by navArgs()

    private var isSettedFavorite = false

    private var requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            takeQuoteWithCamera()
        }
        else {
            Toast.makeText(requireContext(), "Permesso Negato", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        modifyQuoteVM.changeQuoteTitle(args.bookTitle)
        modifyQuoteVM.changeQuoteAuthor(args.bookAuthor)

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentModifyQuoteBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        setObservers()

        binding.modifyQuoteChapterEditText.doOnTextChanged {text, _, _, _ ->
            modifyQuoteVM.changeQuoteChapter(text)
        }
        binding.modifyQuotePagesEditText.doOnTextChanged {text, _, _, _ ->
            modifyQuoteVM.changeQuotePage(text.toString())
        }
        binding.modifyQuoteQuoteEditText.doOnTextChanged { text, _ , _ , _ ->
            modifyQuoteVM.changeQuoteText(text.toString())
            Log.i("Nicosanti", "Do on Text ${text.toString()}")
        }
        binding.modifyQuoteThoughtEditText.doOnTextChanged {text, _, _, _ ->
            modifyQuoteVM.changeQuoteThought(text.toString())
        }

        binding.modifyQuoteSaveButton.setOnClickListener(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
        }, ContextCompat.getMainExecutor(requireContext()))

    }

    private fun setObservers() {
        val currentQuoteObserver = Observer<Quote> { newQuote ->
            Log.i("Nicosanti", "Observer")
            isSettedFavorite = newQuote.favourite

            Log.i("Nicosanti", newQuote.quoteText)
        }
        modifyQuoteVM.currentQuote.observe(viewLifecycleOwner, currentQuoteObserver)

        val currentScannedText = Observer<String?> { newScannedString ->
            if (newScannedString != null) {
                binding.modifyQuoteQuoteEditText.text = Editable.Factory.getInstance().newEditable(newScannedString)
                modifyQuoteVM.onTextScanned(null)
            }
        }
        modifyQuoteVM.currentTextScanned.observe(viewLifecycleOwner, currentScannedText)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.modify_quote_fragment_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (isSettedFavorite) menu[0].setIcon(R.drawable.ic_round_modify_quote_favorite_on)
        else menu[0].setIcon(R.drawable.ic_round_modify_quote_favorite_off)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.modifyQuoteMenuFavoriteItem -> {
                if (isSettedFavorite) item.setIcon(R.drawable.ic_round_modify_quote_favorite_off)
                else item.setIcon(R.drawable.ic_round_modify_quote_favorite_on)
                isSettedFavorite = !isSettedFavorite
                modifyQuoteVM.changeQuoteFavorite(isSettedFavorite)
                true
            }

            R.id.modifyQuoteMenuAddWithCameraItem -> {
                requestPermissionForCamera()
                true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun requestPermissionForCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        else {
            takeQuoteWithCamera()
        }
    }

    private fun takeQuoteWithCamera() {
        findNavController().navigate(ModifyQuoteFragmentDirections.actionModifyQuoteFragmentToQuoteWithCameraFragment())
    }

    override fun onClick(v: View?) {

        when (v) {
            binding.modifyQuoteSaveButton -> {
                if (binding.modifyQuoteQuoteEditText.text.toString() == "") {
                    binding.modifyQuoteQuoteEditText.error = getString(R.string.obligatory_quote_text_string)
                }
                else {
                    modifyQuoteVM.saveQuote()
                    requireActivity().onBackPressed()
                }
            }
        }
    }

}