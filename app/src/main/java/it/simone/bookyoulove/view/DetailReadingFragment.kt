package it.simone.bookyoulove.view

import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.CheckBox
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.databinding.FragmentDetailReadingBinding
import it.simone.bookyoulove.view.dialog.CoverLinkPickerFragment
import it.simone.bookyoulove.view.dialog.DatePickerFragment
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.view.dialog.PagesPickerFragment
import it.simone.bookyoulove.viewmodel.DetailReadingViewModel
import it.simone.bookyoulove.viewmodel.ReadingViewModel


class DetailReadingFragment : Fragment() , View.OnClickListener {

    private var isEditing = false

    private lateinit var binding : FragmentDetailReadingBinding

    private val detailReadingVM : DetailReadingViewModel by viewModels({ this })
    private val readingVM : ReadingViewModel by activityViewModels()

    private val args : DetailReadingFragmentArgs by navArgs()

    private var loadingDialog = LoadingDialogFragment()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) isEditing = savedInstanceState.getBoolean("isEditing")
        setHasOptionsMenu(true)

        childFragmentManager.setFragmentResultListener("startDateKey", this) { _, bundle ->
            val startDateResult = StartDate(bundle.getInt("day"), bundle.getInt("month"), bundle.getInt("year"))
            detailReadingVM.startDate = startDateResult
            detailReadingVM.updateStartDate()
        }

        childFragmentManager.setFragmentResultListener("coverLinkKey",this)  { _, bundle ->
            val coverLinkResult : String? = bundle.getString("settedCoverLink")
            detailReadingVM.coverLink = coverLinkResult.toString()
            detailReadingVM.updateCoverLink()
        }

        childFragmentManager.setFragmentResultListener("pagesKey", this) { _, bundle ->
            val pagesResult : Int = bundle.getInt("settedPages")
            detailReadingVM.newPages = pagesResult
            detailReadingVM.updatePages()
        }

        findNavController().currentDestination?.label = args.detailTitle
    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        binding = FragmentDetailReadingBinding.inflate(inflater, container, false)

        binding.detailReadingCoverImageView.setOnClickListener(this)
        binding.detailReadingStartDateText.setOnClickListener(this)
        binding.detailReadingPages.setOnClickListener(this)
        binding.detailReadingPaperCheckbox.setOnClickListener(this)
        binding.detailReadingEbookCheckbox.setOnClickListener(this)
        binding.detailReadingAudiobookCheckbox.setOnClickListener(this)

        setObservers()

        setEditMode()

        binding.detailReadingTitle.doAfterTextChanged {
            if (detailReadingVM.showedBook != null) {
                detailReadingVM.showedBook!!.title = it.toString()
                detailReadingVM.updateTitle()
            }
        }
        binding.detailReadingAuthor.doAfterTextChanged {
            if (detailReadingVM.showedBook != null) {
                detailReadingVM.showedBook!!.author = it.toString()
                detailReadingVM.updateAuthor()
            }
        }

        return binding.root
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.detail_reading_book_menu, menu)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isEditing" , isEditing)
    }


    private fun setObservers() {
        val accessingDBObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                loadingDialog.show(childFragmentManager, "Loading Dialog")
            }
            else {
                if (loadingDialog.isAdded) {
                    loadingDialog.dismiss()
                    loadingDialog = LoadingDialogFragment()
                }
            }
        }
        detailReadingVM.isAccessingDatabase.observe(viewLifecycleOwner, accessingDBObserver)

        /*
        val exitObserver = Observer<Boolean> { canExit ->
            if (canExit) requireActivity().onBackPressed()
        }
        detailReadingVM.canExit.observe(viewLifecycleOwner, exitObserver)
         */

        val loadedOnceObserver = Observer<Boolean> { loaded ->
            if (!loaded) detailReadingVM.getRequestedBook(args.detailTitle, args.detailAuthor, args.detailTime)
        }
        detailReadingVM.loadedDataOnce.observe(viewLifecycleOwner, loadedOnceObserver)


        val bookObserver = Observer<Book> { newBook ->
            binding.detailReadingTitle.setText(newBook.title)
            binding.detailReadingAuthor.setText(newBook.author)

            binding.detailReadingPaperCheckbox.isChecked = newBook.support?.paperSupport ?: false
            binding.detailReadingEbookCheckbox.isChecked = newBook.support?.ebookSupport ?: false
            binding.detailReadingAudiobookCheckbox.isChecked = newBook.support?.audiobookSupport ?: false
        }
        detailReadingVM.currentBook.observe(viewLifecycleOwner, bookObserver)

        val currentCoverObserver = Observer<String> { newCoverLink ->
            if (newCoverLink != "") Picasso.get().load(newCoverLink).into(binding.detailReadingCoverImageView)
            else Picasso.get().load(R.mipmap.book_cover_placeholder).into(binding.detailReadingCoverImageView)
        }
        detailReadingVM.currentCover.observe(viewLifecycleOwner, currentCoverObserver)

        val updatedStartDateObserver = Observer<String> { newStartDateString ->
            binding.detailReadingStartDateText.text = newStartDateString
        }
        detailReadingVM.currentStartDate.observe(viewLifecycleOwner, updatedStartDateObserver)

        val updatedPagesObserver = Observer<Int> { newPages ->
            binding.detailReadingPages.text = newPages.toString()
        }
        detailReadingVM.currentPages.observe(viewLifecycleOwner, updatedPagesObserver)

        val updatedDatabaseObserver = Observer<Boolean> { isUpdated ->
            if (isUpdated) readingVM.readingUpdated(true)
        }
        detailReadingVM.updatedDatabase.observe(viewLifecycleOwner, updatedDatabaseObserver)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //Change icon and Fragment Mode
        return when (item.itemId) {
            R.id.detailReadingMenuEdit -> {
                if (!isEditing) {
                    item.setIcon(R.drawable.ic_round_save_new_reading_book)
                }

                if (isEditing) {
                    val insertedTitle = binding.detailReadingTitle.text.toString()
                    val insertedAuthor = binding.detailReadingAuthor.text.toString()
                    if (insertedTitle == "" || insertedAuthor == "") {
                        if (insertedTitle == "") binding.detailReadingTitle.error = getString(R.string.new_book_missing_title_error_string)
                        if (insertedAuthor == "") binding.detailReadingAuthor.error = getString(R.string.new_book_missing_author_error_string)
                        val newSnackbar = Snackbar.make(requireView(), R.string.new_book_missing_fields_error_string, Snackbar.LENGTH_SHORT)
                        newSnackbar.setAnchorView(R.id.bottomNavigationView)
                        newSnackbar.show()
                        return true
                    }
                    item.setIcon(R.drawable.ic_round_edit_reading_details)

                    saveUpdatedBook()
                }

                isEditing = !isEditing
                setEditMode()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    //NON RIESCO A IMPOSTARE L'ICONA DEL MENU IN BASE A VALORE PRECEDENTE DI isEditing: TROVA MODO!!

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        /*
            L'indice dell'item deve essere passato come indice dell'item nella gerarchia del layout
            del menu e non come index di view
         */
        val editingItem = menu.getItem(0)
        if (isEditing) editingItem.setIcon(R.drawable.ic_round_save_new_reading_book)
        else editingItem.setIcon(R.drawable.ic_round_edit_reading_details)
    }


    private fun setEditMode() {

        binding.detailReadingCoverImageView.isClickable = isEditing

        binding.detailReadingTitle.run {
            isClickable = isEditing
            isLongClickable = isEditing
            isFocusable = isEditing
            isFocusableInTouchMode = isEditing
            inputType = if (!isEditing) InputType.TYPE_NULL else InputType.TYPE_CLASS_TEXT
        }

        binding.detailReadingAuthor.run {
            isClickable = isEditing
            isLongClickable = isEditing
            isFocusable = isEditing
            isFocusableInTouchMode = isEditing
            inputType = if (!isEditing) InputType.TYPE_NULL else InputType.TYPE_CLASS_TEXT
        }

        binding.detailReadingStartDateText.isClickable = isEditing

        binding.detailReadingPaperCheckbox.isClickable = isEditing
        binding.detailReadingEbookCheckbox.isClickable = isEditing
        binding.detailReadingAudiobookCheckbox.isClickable = isEditing

        binding.detailReadingPages.isClickable = isEditing
    }


    private fun saveUpdatedBook() {
        detailReadingVM.saveModifiedBook()
    }


    override fun onClick(view: View?) {

        when (view) {

            binding.detailReadingCoverImageView -> { CoverLinkPickerFragment().show(childFragmentManager, "Image Link Picker") }

            binding.detailReadingStartDateText -> {
                val args = bundleOf("caller" to START_DATE_SETTER)
                val datePicker = DatePickerFragment()
                datePicker.arguments = args
                datePicker.show(childFragmentManager, "Start Date Picker")
            }

            binding.detailReadingPages -> { PagesPickerFragment().show(childFragmentManager, "Pages Picker") }


            binding.detailReadingPaperCheckbox -> {
                detailReadingVM.showedBook?.support?.paperSupport = (view as CheckBox).isChecked
                detailReadingVM.updateSupport()
            }

            binding.detailReadingEbookCheckbox -> {
                detailReadingVM.showedBook?.support?.ebookSupport = (view as CheckBox).isChecked
                detailReadingVM.updateSupport()
            }

            binding.detailReadingAudiobookCheckbox -> {
                detailReadingVM.showedBook?.support?.audiobookSupport = (view as CheckBox).isChecked
                detailReadingVM.updateSupport()
            }
        }
    }
}