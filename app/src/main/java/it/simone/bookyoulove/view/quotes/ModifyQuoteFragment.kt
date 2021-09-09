package it.simone.bookyoulove.view.quotes

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.common.util.concurrent.ListenableFuture
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Quote
import it.simone.bookyoulove.databinding.FragmentModifyQuoteBinding
import it.simone.bookyoulove.view.setViewEnable
import it.simone.bookyoulove.viewmodel.quotes.ModifyQuoteViewModel


class ModifyQuoteFragment : Fragment(), View.OnClickListener {

    private lateinit var binding : FragmentModifyQuoteBinding
    private val modifyQuoteVM : ModifyQuoteViewModel by viewModels()

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

        if (args.modifyQuote == null) {
            //Aggiunta di Quote in libro
            modifyQuoteVM.setQuoteBookInfo(args.bookId, args.bookTitle!!, args.bookAuthor!!)
        }
        else {
            //Modifica di Quote esistente
            modifyQuoteVM.setModifyQuote(args.modifyQuote!!)
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentModifyQuoteBinding.inflate(inflater, container, false)
        setViewEnable(true, requireActivity())

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
        }
        binding.modifyQuoteThoughtEditText.doOnTextChanged {text, _, _, _ ->
            modifyQuoteVM.changeQuoteThought(text.toString())
        }

        binding.modifyQuoteSaveButton.setOnClickListener(this)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("scannedQuoteKey")?.observe(viewLifecycleOwner) {
            //Triggera la doOnTextChange che salva la quote nell'istanza di quote salvata in VM
            binding.modifyQuoteQuoteEditText.setText(it)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("scannedQuoteKey")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraProviderFuture.addListener({
            cameraProviderFuture.get()
        }, ContextCompat.getMainExecutor(requireContext()))

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun setObservers() {
        val currentQuoteObserver = Observer<Quote> { newQuote ->
            isSettedFavorite = newQuote.favourite

            binding.modifyQuoteChapterEditText.setText(newQuote.quoteChapter)
            binding.modifyQuotePagesEditText.setText(newQuote.quotePage.toString())
            binding.modifyQuoteQuoteEditText.setText(newQuote.quoteText)
            binding.modifyQuoteThoughtEditText.setText(newQuote.quoteThought)
        }
        modifyQuoteVM.currentQuote.observe(viewLifecycleOwner, currentQuoteObserver)

        val isAccessingDatabaseObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                setViewEnable(false, requireActivity())
                binding.modifyQuoteLoading.root.visibility = View.VISIBLE
            }

            else {
                setViewEnable(true, requireActivity())
                binding.modifyQuoteLoading.root.visibility = View.GONE
            }
        }
        modifyQuoteVM.isAccessingDatabase.observe(viewLifecycleOwner, isAccessingDatabaseObserver)

        val canExitWithQuoteObserver = Observer<Quote> {finalQuote ->
            if (args.modifyQuote != null) {
                //Chiamato da detail --> Devo comunicare la modifica del testo
                findNavController().previousBackStackEntry?.savedStateHandle?.set("modifiedQuote", finalQuote)
            }

            findNavController().popBackStack()
        }
        modifyQuoteVM.canExitWithQuote.observe(viewLifecycleOwner, canExitWithQuoteObserver)
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
                }
            }
        }
    }

}