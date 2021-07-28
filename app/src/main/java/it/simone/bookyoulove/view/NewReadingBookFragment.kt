package it.simone.bookyoulove.view

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.StartDate
import it.simone.bookyoulove.databinding.FragmentNewReadingBookBinding
import it.simone.bookyoulove.view.dialog.CoverLinkPickerFragment
import it.simone.bookyoulove.view.dialog.DatePickerFragment
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.view.dialog.PagesPickerFragment
import it.simone.bookyoulove.viewmodel.NewReadingBookViewModel
import it.simone.bookyoulove.viewmodel.ReadingViewModel
import it.simone.bookyoulove.viewmodel.UpdatedDatabaseViewModel


class NewReadingBookFragment : Fragment() , View.OnClickListener {

    private lateinit var binding: FragmentNewReadingBookBinding

    private val newBookViewModel: NewReadingBookViewModel by viewModels({this})
    private val readingVM: ReadingViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childFragmentManager.setFragmentResultListener("startDateKey", this) { _, bundle ->
            val startDateResult = StartDate(bundle.getInt("day"), bundle.getInt("month"), bundle.getInt("year"))
            newBookViewModel.newBookStartDate = startDateResult
            newBookViewModel.updateStartDate()
        }

        childFragmentManager.setFragmentResultListener("coverLinkKey",this)  { _, bundle ->
            val coverLinkResult : String? = bundle.getString("settedCoverLink")
            newBookViewModel.newBookCoverLink = coverLinkResult!!
            newBookViewModel.updateLink()
        }

        childFragmentManager.setFragmentResultListener("pagesKey", this) { _, bundle ->
            val pagesResult : Int = bundle.getInt("settedPages")
            newBookViewModel.newBookPages = pagesResult
            newBookViewModel.updatePages()
        }
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

        newBookViewModel.updateStartDate()
        newBookViewModel.updatePages()
        newBookViewModel.updateLink()
        newBookViewModel.updateAuthorList()

        return binding.root
    }


    private fun setObservers() {
        val startDateObserver = Observer<String> { newStartDateString ->
            binding.newBookStartDateText.text = newStartDateString
        }
        newBookViewModel.currentStartDateString.observe(viewLifecycleOwner, startDateObserver)

        val pagesObserver = Observer<Int> { newPagesValue ->
            binding.newBookPagesInput.text = newPagesValue.toString()
        }
        newBookViewModel.currentPages.observe(viewLifecycleOwner, pagesObserver)

        val linkObserver = Observer<String> { newLink ->
            if (newLink != "") Picasso.get().load(newLink).error(R.mipmap.error_cover_image).into(binding.newBookCoverImageView)
            else Picasso.get().load(R.mipmap.book_cover_placeholder).into(binding.newBookCoverImageView)
        }
        newBookViewModel.currentLink.observe(viewLifecycleOwner, linkObserver)

        val authorListObserver = Observer<Array<String>> { newAuthorList ->
            val authorAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, newAuthorList)
            binding.newBookAuthorInput.setAdapter(authorAdapter)
        }
        newBookViewModel.currentAuthorArray.observe(viewLifecycleOwner, authorListObserver)

        val updatingDatabaseObserver = Observer<Boolean> { newUpdating ->
            showProgressBar()
        }
        newBookViewModel.isAccessingDatabase.observe(viewLifecycleOwner, updatingDatabaseObserver)

        val exitObserver = Observer<Boolean> { canExit ->
            if (canExit) {
                readingVM.readingUpdated(true)
                Log.i("Nicosanti", "NewReadingFragment : Exit & Updated")
                requireActivity().onBackPressed()
            }
        }
        newBookViewModel.canExit.observe(viewLifecycleOwner, exitObserver)
    }


    private fun showProgressBar() {
        Log.i("Nicosanti", "Progress")
        LoadingDialogFragment().show(childFragmentManager, "Loading Fragment")
    }


    override fun onClick(view: View?) {

        when (view) {
            binding.newBookCoverImageView -> CoverLinkPickerFragment().show(
                childFragmentManager,
                "Cover Link Picker"
            )

            binding.newBookPagesInput -> PagesPickerFragment().show(
                childFragmentManager,
                "Pages Picker"
            )

            binding.newBookStartDateText -> {
                val newDatePicker = DatePickerFragment()
                val args = Bundle()
                args.putInt("caller", START_DATE_SETTER)
                newDatePicker.arguments = args

                newDatePicker.show(childFragmentManager, "Start Date Picker")
            }

            binding.newBookPaperCheckbox -> newBookViewModel.newBookSupport[PAPER_SUPPORT] = (view as CheckBox).isChecked

            binding.newBookEbookCheckbox -> newBookViewModel.newBookSupport[EBOOK_SUPPORT] = (view as CheckBox).isChecked

            binding.newBookAudiobookCheckbox -> newBookViewModel.newBookSupport[AUDIOBOOK_SUPPORT] = (view as CheckBox).isChecked

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
                    newBookViewModel.addNewBook(binding.newBookTitleInput.text.toString(), binding.newBookAuthorInput.text.toString())
                }
            }
        }
    }

}
