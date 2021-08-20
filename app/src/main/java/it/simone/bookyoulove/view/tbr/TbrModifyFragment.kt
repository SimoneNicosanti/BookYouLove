package it.simone.bookyoulove.view.tbr


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.common.util.concurrent.ListenableFuture
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.databinding.FragmentTbrModifyBinding
import it.simone.bookyoulove.view.dialog.AlertDialogFragment
import it.simone.bookyoulove.view.dialog.CoverLinkPickerFragment
import it.simone.bookyoulove.viewmodel.ISBN_FIND_ITEM_ERROR
import it.simone.bookyoulove.viewmodel.ISBN_INTERNET_ACCESS_ERROR
import it.simone.bookyoulove.viewmodel.ISBN_NO_ERROR
import it.simone.bookyoulove.viewmodel.TbrModifyViewModel


class TbrModifyFragment : Fragment(), View.OnClickListener {

    private lateinit var binding : FragmentTbrModifyBinding

    private val args : TbrModifyFragmentArgs by navArgs()

    private val tbrModifyVM : TbrModifyViewModel by viewModels()

    private var requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            takeIsbnWithCamera()
        }
        else {
            Toast.makeText(requireContext(), "Permesso Negato", Toast.LENGTH_SHORT).show()
        }
    }


    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (args.tbrModifyShowedBookInfo != null) {
            tbrModifyVM.getTbrModifyBook(args.tbrModifyShowedBookInfo!!.bookId)
        }

        tbrModifyVM.loadAuthorList()

        setHasOptionsMenu(true)

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentTbrModifyBinding.inflate(inflater, container, false)

        setObservers()

        binding.tbrModifyTitleEditText.doOnTextChanged { text, _, _, _ ->
            tbrModifyVM.changeTitleText(text)
        }

        binding.tbrModifyAuthorEditText.doOnTextChanged {text, _ , _ , _ ->
            tbrModifyVM.changeAuthorText(text)
        }

        binding.tbrModifyPagesEditText.doOnTextChanged {text , _ , _ , _ ->
            tbrModifyVM.changePages(text)
        }

        binding.tbrModifySaveButton.setOnClickListener(this)
        binding.tbrModifyCoverImageView.setOnClickListener(this)

        childFragmentManager.setFragmentResultListener("coverLinkKey", this) { _, bundle ->
            val coverLink = bundle.getString("settedCoverLink")

            if (coverLink != "") Picasso.get().load(coverLink)
                .placeholder(R.drawable.book_cover_place_holder)
                .error(R.drawable.cover_not_found)
                .into(binding.tbrModifyCoverImageView)

            else Picasso.get().load(R.drawable.book_cover_place_holder).into(binding.tbrModifyCoverImageView)

            tbrModifyVM.changeCoverLink(coverLink!!)
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("scannedIsbnKey")?.observe(viewLifecycleOwner) { scannedIsbn ->
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("scannedIsbnKey")
            tbrModifyVM.findBookByIsbn(scannedIsbn)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun setObservers() {

        val isAccessingObserver = Observer<Boolean> {isAccessing ->
            if (isAccessing) {
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.tbrModifyLoading.root.visibility = View.VISIBLE
            }

            else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.tbrModifyLoading.root.visibility = View.GONE
            }
        }
        tbrModifyVM.isAccessing.observe(viewLifecycleOwner, isAccessingObserver)

        val authorArrayObserver = Observer<Array<String>> {
            val authorAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, it)
            binding.tbrModifyAuthorEditText.setAdapter(authorAdapter)
        }
        tbrModifyVM.currentAuthorArray.observe(viewLifecycleOwner, authorArrayObserver)

        val currentBookObserver = Observer<Book> {

            binding.tbrModifyTitleEditText.setText(it.title)
            binding.tbrModifyAuthorEditText.setText(it.author)
            binding.tbrModifyPagesEditText.setText(it.pages.toString())

            if (it.coverName != "") Picasso.get().load(it.coverName)
                .placeholder(R.drawable.book_cover_place_holder)
                .error(R.drawable.cover_not_found)
                .into(binding.tbrModifyCoverImageView)

            else Picasso.get().load(R.drawable.book_cover_place_holder).into(binding.tbrModifyCoverImageView)
        }
        tbrModifyVM.currentTbrBook.observe(viewLifecycleOwner, currentBookObserver)

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
                    tbrModifyVM.saveTbrBook()
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
        return if (item.itemId == R.id.tbrModifyFragmentMenuScanItem) {
            requestPermissionForCamera()
            true
        }
        else super.onOptionsItemSelected(item)
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