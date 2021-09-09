package it.simone.bookyoulove.view.reading

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.CheckBox
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.Constants.AUDIOBOOK_SUPPORT
import it.simone.bookyoulove.Constants.EBOOK_SUPPORT
import it.simone.bookyoulove.Constants.ISBN_FIND_ITEM_ERROR
import it.simone.bookyoulove.Constants.ISBN_INTERNET_ACCESS_ERROR
import it.simone.bookyoulove.Constants.ISBN_NO_ERROR
import it.simone.bookyoulove.Constants.PAPER_SUPPORT
import it.simone.bookyoulove.Constants.READING_BOOK_STATE
import it.simone.bookyoulove.Constants.START_DATE_SETTER
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.databinding.FragmentNewReadingBookBinding
import it.simone.bookyoulove.model.GoogleBooksApi
import it.simone.bookyoulove.utilsClass.DateFormatClass
import it.simone.bookyoulove.utilsClass.MyPicasso
import it.simone.bookyoulove.view.dialog.AlertDialogFragment
import it.simone.bookyoulove.view.dialog.CoverLinkPickerFragment
import it.simone.bookyoulove.view.dialog.DatePickerFragment
import it.simone.bookyoulove.view.setViewEnable
import it.simone.bookyoulove.view.tbr.TbrModifyFragmentDirections
import it.simone.bookyoulove.viewmodel.BookListViewModel
import it.simone.bookyoulove.viewmodel.ModifyBookViewModel


class  NewReadingBookFragment : Fragment() , View.OnClickListener {

    private lateinit var binding: FragmentNewReadingBookBinding

    private val newReadingVM: ModifyBookViewModel by viewModels()
    private val readingVM: BookListViewModel by activityViewModels()

    private val args : NewReadingBookFragmentArgs by navArgs()


    private var requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            takeIsbnWithCamera()
        }
    }

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childFragmentManager.setFragmentResultListener("startDateKey", this) { _, bundle ->
            val startDateResult = bundle.getLong("dateMillis")
            newReadingVM.modifyStartDate(startDateResult)
            binding.newBookStartDateText.text = DateFormatClass(requireContext()).computeDateString(startDateResult)
        }

        childFragmentManager.setFragmentResultListener("coverLinkKey",this)  { _, bundle ->
            val coverLinkResult : String? = bundle.getString("settedCoverLink")
            coverLinkResult?.let { MyPicasso().putImageIntoView(it, binding.newBookCoverImageView)}

            newReadingVM.modifyCover(coverLinkResult!!)
        }


        if (args.readingModifyBook != null) {
            newReadingVM.setBookToModify(args.readingModifyBook!!)
        }
        else {
            newReadingVM.prepareNewBook(READING_BOOK_STATE)
        }
        newReadingVM.loadAuthorArray()

        setHasOptionsMenu(true)
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNewReadingBookBinding.inflate(inflater, container, false)
        setViewEnable(true, requireActivity())

        binding.newBookCoverImageView.setOnClickListener(this)
        binding.newBookStartDateText.setOnClickListener(this)
        binding.newBookPaperCheckbox.setOnClickListener(this)
        binding.newBookEbookCheckbox.setOnClickListener(this)
        binding.newBookAudiobookCheckbox.setOnClickListener(this)
        binding.newBookSaveButton.setOnClickListener(this)

        setObservers()

        binding.newBookTitleInput.doOnTextChanged { text, _, _, _ ->
            newReadingVM.modifyTitle(text)
        }
        binding.newBookAuthorInput.doOnTextChanged { text, _, _, _ ->
            newReadingVM.modifyAuthor(text)
        }

        binding.newBookPagesInput.doOnTextChanged { text, _, _, _ ->
            newReadingVM.modifyPages(text.toString())
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("scannedIsbnKey")?.observe(viewLifecycleOwner) { scannedIsbn ->
            //Snackbar.make(requireView(), scannedIsbn, Snackbar.LENGTH_LONG).show()
            newReadingVM.askBookByIsbn(scannedIsbn)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("scannedIsbnKey")
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<GoogleBooksApi.NetworkBook>("selectedGoogleBook")?.observe(viewLifecycleOwner) { selectedNetworkBook ->
            newReadingVM.onNetworkBookReceived(selectedNetworkBook, ISBN_NO_ERROR)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<GoogleBooksApi.NetworkBook>("selectedGoogleBook")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraProviderFuture.addListener({ cameraProviderFuture.get() }, ContextCompat.getMainExecutor(requireContext()))

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }


    private fun setObservers() {

        val authorListObserver = Observer<Array<String>> { newAuthorList ->
            val authorAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, newAuthorList)
            binding.newBookAuthorInput.setAdapter(authorAdapter)
        }
        newReadingVM.currentAuthorArray.observe(viewLifecycleOwner, authorListObserver)

        val isAccessingDatabaseObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                setViewEnable(false, requireActivity())
                binding.modifyReadingLoading.root.visibility = View.VISIBLE
            }

            else {
                setViewEnable(true, requireActivity())
                binding.modifyReadingLoading.root.visibility = View.GONE
            }
        }
        newReadingVM.isAccessing.observe(viewLifecycleOwner, isAccessingDatabaseObserver)

        val exitObserver = Observer<Book> { finalBook ->
            Log.d("Nicosanti", "Final Book")
            if (args.readingModifyBook != null) {
                //Chiamato da Detail
                //readingVM.readingUpdated(true)
                readingVM.notifyArrayItemChanged(finalBook)
                findNavController().previousBackStackEntry?.savedStateHandle?.set("modifiedBook", finalBook)
            }
            else {
                //Chiamato da Reading
                readingVM.notifyNewArrayItem(finalBook)
            }
            //Torno indietro dopo salvataggio
            findNavController().popBackStack()
        }
        newReadingVM.canExitWithBook.observe(viewLifecycleOwner, exitObserver)


        val currentBookObserver = Observer<Book> { currentBook ->
            MyPicasso().putImageIntoView(currentBook.coverName, binding.newBookCoverImageView)

            binding.newBookTitleInput.setText(currentBook.title)
            binding.newBookAuthorInput.setText(currentBook.author)

            binding.newBookStartDateText.text = DateFormatClass(requireContext()).computeDateString(currentBook.startDate)

            binding.newBookPaperCheckbox.isChecked = currentBook.support?.paperSupport ?: false
            binding.newBookEbookCheckbox.isChecked = currentBook.support?.ebookSupport ?: false
            binding.newBookAudiobookCheckbox.isChecked = currentBook.support?.audiobookSupport ?: false

            binding.newBookPagesInput.setText(currentBook.pages.toString())
        }
        newReadingVM.currentBook.observe(viewLifecycleOwner, currentBookObserver)

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
                newReadingVM.handledInternetError(ISBN_NO_ERROR)
            }
        }
        newReadingVM.internetAccessError.observe(viewLifecycleOwner, internetAccessErrorObserver)

    }


    override fun onClick(view: View?) {

        when (view) {
            binding.newBookCoverImageView -> CoverLinkPickerFragment().show(
                childFragmentManager,
                "Cover Link Picker"
            )

            binding.newBookStartDateText -> {
                val newDatePicker = DatePickerFragment()
                newDatePicker.arguments = bundleOf("caller" to START_DATE_SETTER)
                newDatePicker.show(childFragmentManager, "Start Date Picker")
            }

            binding.newBookPaperCheckbox, binding.newBookEbookCheckbox, binding.newBookAudiobookCheckbox -> {
                view as CheckBox
                val paperSupport = binding.newBookPaperCheckbox.isChecked
                val ebookSupport = binding.newBookEbookCheckbox.isChecked
                val audiobookSupport = binding.newBookAudiobookCheckbox.isChecked
                val supportMap = mapOf(PAPER_SUPPORT to paperSupport, EBOOK_SUPPORT to ebookSupport, AUDIOBOOK_SUPPORT to audiobookSupport)
                newReadingVM.modifySupport(supportMap)
            }


            binding.newBookSaveButton -> {
                val titleName = binding.newBookTitleInput.text.toString()
                val authorName = binding.newBookAuthorInput.text.toString()

                if (titleName == "" || authorName == "") {
                    if (titleName == "") binding.newBookTitleInput.error = getString(R.string.new_book_missing_title_error_string)
                    if (authorName == "") binding.newBookAuthorInput.error = getString(R.string.new_book_missing_author_error_string)

                    val newSnackbar = Snackbar.make(requireView(), R.string.new_book_missing_fields_error_string, Snackbar.LENGTH_SHORT)
                    newSnackbar.setAnchorView(R.id.bottomNavigationView)
                    newSnackbar.show()
                }

                else {
                    newReadingVM.addNewBook()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.modify_reading_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.modifyReadingMenuScanItem -> {
                requestPermissionForCamera()
                true
            }
            R.id.modifyReadingMenuSearchOnlineItem -> {
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
        findNavController().navigate(NewReadingBookFragmentDirections.actionGlobalTakeBookIsbnFragment())
    }

}
