package it.simone.bookyoulove.view.reading

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.databinding.FragmentNewReadingBookBinding
import it.simone.bookyoulove.view.*
import it.simone.bookyoulove.view.dialog.CoverLinkPickerFragment
import it.simone.bookyoulove.view.dialog.DatePickerFragment
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.view.dialog.PagesPickerFragment
import it.simone.bookyoulove.viewmodel.NewReadingBookViewModel
import it.simone.bookyoulove.viewmodel.ReadingViewModel
import java.time.Month
import java.time.format.TextStyle
import java.util.*


class NewReadingBookFragment : Fragment() , View.OnClickListener {

    private lateinit var binding: FragmentNewReadingBookBinding

    private val newReadingVM: NewReadingBookViewModel by viewModels()
    private val readingVM: ReadingViewModel by activityViewModels()

    private val args : NewReadingBookFragmentArgs by navArgs()

    private var loadingDialog = LoadingDialogFragment()


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

        childFragmentManager.setFragmentResultListener("pagesKey", this) { _, bundle ->
            val pagesResult : Int = bundle.getInt("settedPages")
            binding.newBookPagesInput.text = pagesResult.toString()
            newReadingVM.updatePages(pagesResult)
        }

        if (args.readingModifyKeyTitle != null) {
            newReadingVM.loadReadingBookToModify(args.readingModifyKeyTitle!!, args.readingModifyKeyAuthor!!, args.readingModifyTime)
        }
        newReadingVM.loadAuthorArray()
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
        binding.newBookPagesInput.setOnClickListener (this)
        binding.newBookSaveButton.setOnClickListener(this)

        setObservers()

        binding.newBookTitleInput.doOnTextChanged { text, _, _, _ ->
            newReadingVM.updateTitle(text)
        }
        binding.newBookAuthorInput.doOnTextChanged { text, _, _, _ ->
            newReadingVM.updateAuthor(text)
        }

        return binding.root
    }


    private fun setObservers() {

        val authorListObserver = Observer<Array<String>> { newAuthorList ->
            val authorAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, newAuthorList)
            binding.newBookAuthorInput.setAdapter(authorAdapter)
        }
        newReadingVM.currentAuthorArray.observe(viewLifecycleOwner, authorListObserver)

        val isAccessingDatabaseObserver = Observer<Boolean> { isAccessing ->
            if (isAccessing) {
                loadingDialog.showNow(childFragmentManager, "Loading Dialog")
            }
            else {
                if (loadingDialog.isAdded) {
                    loadingDialog.dismiss()
                    loadingDialog = LoadingDialogFragment()
                }
            }
        }
        newReadingVM.isAccessingDatabase.observe(viewLifecycleOwner, isAccessingDatabaseObserver)

        //TODO("Rivedi aggiornamento reading list : Problema non so se chi ha chiamato lo ha fatto da detail o da new --> nel dubbio posso fare upload totale")
        val exitObserver = Observer<Boolean> { canExit ->
            if (canExit) {
                readingVM.readingUpdated(true)

                val navController = findNavController()
                val action = NewReadingBookFragmentDirections.actionGlobalReadingFragment()
                navController.navigate(action)
            }
        }
        newReadingVM.canExit.observe(viewLifecycleOwner, exitObserver)


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

            binding.newBookPagesInput.text = currentBook.pages.toString()
        }
        newReadingVM.currentBook.observe(viewLifecycleOwner, currentBookObserver)
    }




    override fun onClick(view: View?) {

        when (view) {
            binding.newBookCoverImageView -> CoverLinkPickerFragment().show(
                childFragmentManager,
                "Cover Link Picker"
            )

            //TODO("Sostituire number picker con EditText che prende solo numeri : non ho problemi con tipo di input")
            binding.newBookPagesInput -> PagesPickerFragment().show(
                childFragmentManager,
                "Pages Picker"
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

}
