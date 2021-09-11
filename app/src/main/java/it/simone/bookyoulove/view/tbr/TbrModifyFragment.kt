package it.simone.bookyoulove.view.tbr


import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.common.util.concurrent.ListenableFuture
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.Constants.ISBN_FIND_ITEM_ERROR
import it.simone.bookyoulove.Constants.ISBN_INTERNET_ACCESS_ERROR
import it.simone.bookyoulove.Constants.ISBN_NO_ERROR
import it.simone.bookyoulove.Constants.TBR_BOOK_STATE
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.databinding.FragmentTbrModifyBinding
import it.simone.bookyoulove.model.GoogleBooksApi
import it.simone.bookyoulove.utilsClass.MyPicasso
import it.simone.bookyoulove.view.dialog.AlertDialogFragment
import it.simone.bookyoulove.view.dialog.CoverLinkPickerFragment
import it.simone.bookyoulove.view.setViewEnable
import it.simone.bookyoulove.viewmodel.ModifyBookViewModel


class TbrModifyFragment : Fragment(), View.OnClickListener {

    private lateinit var binding : FragmentTbrModifyBinding

    private val args : TbrModifyFragmentArgs by navArgs()

    private val tbrModifyVM : ModifyBookViewModel by viewModels()

    private var requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            takeIsbnWithCamera()
        }
    }


    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (args.tbrModifyShowedBookInfo != null) {
            tbrModifyVM.getTbrModifyBook(args.tbrModifyShowedBookInfo!!.bookId)
        }
        else {
            tbrModifyVM.prepareNewBook(TBR_BOOK_STATE)
        }

        tbrModifyVM.loadAuthorArray()

        setHasOptionsMenu(true)

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentTbrModifyBinding.inflate(inflater, container, false)
        //setViewEnable(true, requireActivity())

        setObservers()

        binding.tbrModifyTitleEditText.doOnTextChanged { text, _, _, _ ->
            tbrModifyVM.modifyTitle(text)
        }

        binding.tbrModifyAuthorEditText.doOnTextChanged {text, _ , _ , _ ->
            tbrModifyVM.modifyAuthor(text)
        }

        binding.tbrModifyPagesEditText.doOnTextChanged {text , _ , _ , _ ->
            tbrModifyVM.modifyPages(text.toString())
        }

        binding.tbrModifySaveButton.setOnClickListener(this)
        binding.tbrModifyCoverImageView.setOnClickListener(this)

        childFragmentManager.setFragmentResultListener("coverLinkKey", this) { _, bundle ->
            val coverLink = bundle.getString("settedCoverLink")

            coverLink?.let { MyPicasso().putImageIntoView(it, binding.tbrModifyCoverImageView)}

            tbrModifyVM.modifyCover(coverLink!!)
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("scannedIsbnKey")?.observe(viewLifecycleOwner) { scannedIsbn ->
            tbrModifyVM.askBookByIsbn(scannedIsbn)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("scannedIsbnKey")
        }
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<GoogleBooksApi.NetworkBook>("selectedGoogleBook")?.observe(viewLifecycleOwner) { selectedNetworkBook ->
            tbrModifyVM.onNetworkBookReceived(selectedNetworkBook, ISBN_NO_ERROR)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<GoogleBooksApi.NetworkBook>("selectedGoogleBook")
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

        val isAccessingObserver = Observer<Boolean> {isAccessing ->
            if (isAccessing) {
                setViewEnable(false, requireActivity())
                binding.tbrModifyLoading.root.visibility = View.VISIBLE
            }

            else {
                setViewEnable(true, requireActivity())
                binding.tbrModifyLoading.root.visibility = View.GONE
            }
        }
        tbrModifyVM.isAccessing.observe(viewLifecycleOwner, isAccessingObserver)

        val authorArrayObserver = Observer<Array<String>> {
            val authorAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, it)
            binding.tbrModifyAuthorEditText.setAdapter(authorAdapter)
        }
        tbrModifyVM.currentAuthorArray.observe(viewLifecycleOwner, authorArrayObserver)

        val currentBookObserver = Observer<Book> {

            binding.tbrModifyTitleEditText.setText(it.title)
            binding.tbrModifyAuthorEditText.setText(it.author)
            binding.tbrModifyPagesEditText.setText(it.pages.toString())

            MyPicasso().putImageIntoView(it.coverName, binding.tbrModifyCoverImageView)
        }
        tbrModifyVM.currentBook.observe(viewLifecycleOwner, currentBookObserver)

        val canExitWithBookObserver = Observer<Book> {
            val exitKey : String = if (args.tbrModifyShowedBookInfo == null) {
                "newTbrBookKey"
            } else {
                "modifyTbrBookKey"
            }
            findNavController().previousBackStackEntry?.savedStateHandle?.set(exitKey, it)
            findNavController().popBackStack()
        }
        tbrModifyVM.canExitWithBook.observe(viewLifecycleOwner, canExitWithBookObserver)

        val internetAccessErrorObserver = Observer<Int> { errorCode ->
            if (errorCode != ISBN_NO_ERROR) {
                val myAlert = AlertDialogFragment()
                val args = when (errorCode) {
                    ISBN_INTERNET_ACCESS_ERROR -> bundleOf("alertDialogTitleKey" to resources.getString(R.string.no_internet_connection_string))
                    ISBN_FIND_ITEM_ERROR -> bundleOf("alertDialogTitleKey" to getString(R.string.no_book_found_string))
                    else -> bundleOf("alertDialogTitleKey" to "")
                }
                myAlert.arguments = args
                myAlert.showNow(childFragmentManager, "Alert Dialog")
                tbrModifyVM.handledInternetError(ISBN_NO_ERROR)
            }
        }
        tbrModifyVM.internetAccessError.observe(viewLifecycleOwner, internetAccessErrorObserver)
    }

    override fun onClick(v: View?) {

        when (v) {

            binding.tbrModifyCoverImageView -> {
               CoverLinkPickerFragment().show(childFragmentManager, "Cover Link Picker")
            }

            binding.tbrModifySaveButton -> {
                if (binding.tbrModifyTitleEditText.text.toString() != "" && binding.tbrModifyAuthorEditText.text.toString() != "") {
                    tbrModifyVM.addNewBook()
                }

                else {
                    if (binding.tbrModifyTitleEditText.text.toString() == "") binding.tbrModifyTitleEditText.error = getString(R.string.new_book_missing_title_error_string)
                    if (binding.tbrModifyAuthorEditText.text.toString() == "") binding.tbrModifyAuthorEditText.error = getString(R.string.new_book_missing_author_error_string)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tbr_modify_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.tbrModifyFragmentMenuScanItem -> {
                requestPermissionForCamera()
                true
            }
            R.id.tbrModifyFragmentMenuSearchOnlineItem -> {
                findNavController().navigate(TbrModifyFragmentDirections.actionGlobalGoogleBooksSearch())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun requestPermissionForCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        else {
            takeIsbnWithCamera()
        }
    }

    private fun takeIsbnWithCamera() {
        findNavController().navigate(TbrModifyFragmentDirections.actionGlobalTakeBookIsbnFragment())
    }

}