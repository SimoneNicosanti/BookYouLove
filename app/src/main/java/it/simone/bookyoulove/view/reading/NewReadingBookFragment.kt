package it.simone.bookyoulove.view.reading

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
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
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.databinding.FragmentNewReadingBookBinding
import it.simone.bookyoulove.view.*
import it.simone.bookyoulove.view.dialog.*
import it.simone.bookyoulove.viewmodel.*
import java.time.Month
import java.time.format.TextStyle
import java.util.*


class NewReadingBookFragment : Fragment() , View.OnClickListener {

    private lateinit var binding: FragmentNewReadingBookBinding

    private val newReadingVM: NewReadingBookViewModel by viewModels()
    private val readingVM: ReadingViewModel by activityViewModels()

    private val args : NewReadingBookFragmentArgs by navArgs()


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

        childFragmentManager.setFragmentResultListener("startDateKey", this) { _, bundle ->
            val startDateResult = StartDate(bundle.getInt("day"), bundle.getInt("month"), bundle.getInt("year"))
            newReadingVM.updateStartDate(startDateResult)
            binding.newBookStartDateText.text = computeStartDateString(startDateResult)
        }

        childFragmentManager.setFragmentResultListener("coverLinkKey",this)  { _, bundle ->
            val coverLinkResult : String? = bundle.getString("settedCoverLink")

            if (coverLinkResult != "") Picasso.get().load(coverLinkResult).placeholder(R.drawable.book_cover_place_holder).error(
                R.drawable.cover_not_found).into(binding.newBookCoverImageView)
            else Picasso.get().load(R.drawable.book_cover_place_holder).into(binding.newBookCoverImageView)

            newReadingVM.updateCoverLink(coverLinkResult!!)
        }


        if (args.readingModifyBook != null) {
            newReadingVM.setBookToModify(args.readingModifyBook!!)
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

        binding.newBookCoverImageView.setOnClickListener(this)
        binding.newBookStartDateText.setOnClickListener(this)
        binding.newBookPaperCheckbox.setOnClickListener(this)
        binding.newBookEbookCheckbox.setOnClickListener(this)
        binding.newBookAudiobookCheckbox.setOnClickListener(this)
        //binding.newBookPagesInput.setOnClickListener (this)
        binding.newBookSaveButton.setOnClickListener(this)

        setObservers()

        binding.newBookTitleInput.doOnTextChanged { text, _, _, _ ->
            newReadingVM.updateTitle(text)
        }
        binding.newBookAuthorInput.doOnTextChanged { text, _, _, _ ->
            newReadingVM.updateAuthor(text)
        }

        binding.newBookPagesInput.doOnTextChanged { text, _, _, _ ->
            newReadingVM.updatePages(text.toString())
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("scannedIsbnKey")?.observe(viewLifecycleOwner) { scannedIsbn ->
            //Snackbar.make(requireView(), scannedIsbn, Snackbar.LENGTH_LONG).show()
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("scannedIsbnKey")
            newReadingVM.findBookByIsbn(scannedIsbn)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
        }, ContextCompat.getMainExecutor(requireContext()))

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }


    private fun setObservers() {

        val authorListObserver = Observer<Array<String>> { newAuthorList ->
            val authorAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, newAuthorList)
            binding.newBookAuthorInput.setAdapter(authorAdapter)
        }
        newReadingVM.currentAuthorArray.observe(viewLifecycleOwner, authorListObserver)

        val isAccessingDatabaseObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.modifyReadingLoading.root.visibility = View.VISIBLE
            }

            else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.modifyReadingLoading.root.visibility = View.GONE
            }
        }
        newReadingVM.isAccessingDatabase.observe(viewLifecycleOwner, isAccessingDatabaseObserver)

        val exitObserver = Observer<Book> { finalBook ->
            Log.d("Nicosanti", "Final Book")
            if (args.readingModifyBook != null) {
                //Chiamato da Detail
                readingVM.readingUpdated(true)

                findNavController().previousBackStackEntry?.savedStateHandle?.set("modifiedBook", finalBook)
            }
            else {
                //Chiamato da Reading
                readingVM.notifyNewReadingBook(finalBook)
            }
            //Torno indietro dopo salvataggio
            findNavController().popBackStack()
        }
        newReadingVM.canExitWithBook.observe(viewLifecycleOwner, exitObserver)


        val currentBookObserver = Observer<Book> { currentBook ->
            if (currentBook.coverName != "") Picasso.get().load(currentBook.coverName).placeholder(R.drawable.book_cover_place_holder).error(
                R.drawable.cover_not_found).into(binding.newBookCoverImageView)
            else Picasso.get().load(R.drawable.book_cover_place_holder).into(binding.newBookCoverImageView)

            binding.newBookTitleInput.setText(currentBook.title)
            binding.newBookAuthorInput.setText(currentBook.author)

            binding.newBookStartDateText.text = computeStartDateString(currentBook.startDate)

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

            /*
            binding.newBookPagesInput -> PagesPickerFragment().show(
                childFragmentManager,
                "Pages Picker"
            )*/

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
                val supportMap = mapOf<String, Boolean>(PAPER_SUPPORT to paperSupport, EBOOK_SUPPORT to ebookSupport, AUDIOBOOK_SUPPORT to audiobookSupport)
                newReadingVM.updateSupport(supportMap)
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


    private fun computeStartDateString(startDate: StartDate?): String {
        return "${startDate!!.startDay} ${
            Month.of(startDate.startMonth).getDisplayName(
                TextStyle.FULL, Locale.getDefault()).capitalize(Locale.getDefault())
        } ${startDate.startYear}"
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
