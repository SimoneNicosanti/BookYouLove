package it.simone.bookyoulove.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.database.entity.EndDate
import it.simone.bookyoulove.databinding.FragmentEndBookDialogBinding
import it.simone.bookyoulove.view.END_DATE_SETTER
import it.simone.bookyoulove.viewmodel.ReadingViewModel
import java.lang.IllegalStateException
import java.time.Month
import java.time.format.TextStyle
import java.util.*


class EndBookDialogFragment : DialogFragment() , View.OnClickListener {

    private lateinit var binding: FragmentEndBookDialogBinding

    private var endDate : EndDate
    private var settedRate : Float = 0F



    init {
        val cal = Calendar.getInstance()
        endDate = EndDate(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childFragmentManager.setFragmentResultListener("endDateKey", this) { _, bundle ->
            val endDay = bundle.getInt("day")
            val endMonth = bundle.getInt("month")
            val endYear = bundle.getInt("year")

            endDate = EndDate(endDay, endMonth, endYear)

            showDate()
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val newDialog = activity?.let {
            val inflater = requireActivity().layoutInflater
            binding = FragmentEndBookDialogBinding.inflate(inflater)

            binding.endBookDialogEndDateText.setOnClickListener(this)
            restoreStateView(savedInstanceState)
            showDate()

            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.end_book_dialog_title)
            /* Customizzazione del titolo: Per centrarlo
            val titleTextView = TextView(requireContext())
            titleTextView.gravity = Gravity.CENTER
            titleTextView.text = getString(R.string.end_book_dialog_title)
            builder.setCustomTitle(titleTextView)
            */
            builder.setView(binding.root)

            builder.setPositiveButton(R.string.confirm_string) { dialog, _ ->
                presentResults()
                dialog.cancel()
            }

            builder.setNegativeButton(R.string.back_string) { dialog, _ ->
                dialog.cancel()
            }

            builder.create()
        }

        return newDialog ?: throw IllegalStateException("Activity Cannot Be Null")
    }

    private fun presentResults() {
        val returnBundle = Bundle()
        returnBundle.putInt("endDay" , endDate.endDay)
        returnBundle.putInt("endMonth", endDate.endMonth)
        returnBundle.putInt("endYear", endDate.endYear)

        returnBundle.putFloat("settedRate" , binding.endBookDialogRateBar.rating)

        setFragmentResult("endBookInfo" , returnBundle)
    }


    private fun showDate() {
        val dateToShow = "${endDate.endDay} ${Month.of(endDate.endMonth).getDisplayName(TextStyle.FULL, Locale.getDefault())} ${endDate.endYear}"
        binding.endBookDialogEndDateText.text = dateToShow
    }


    override fun onClick(view: View?) {

        when (view) {
            binding.endBookDialogEndDateText -> {
                val args = bundleOf("caller" to END_DATE_SETTER)
                args.putAll(arguments)
                val datePickerFragment = DatePickerFragment()
                datePickerFragment.arguments = args
                datePickerFragment.show(childFragmentManager, "Date Picker")
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putIntegerArrayList("endDateList", arrayListOf(endDate.endDay, endDate.endMonth, endDate.endYear))
        outState.putFloat("settedRate", binding.endBookDialogRateBar.rating)
    }


    private fun restoreStateView(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {

            val endDateArray = savedInstanceState.getIntegerArrayList("endDateList")
            if (endDateArray != null && endDateArray[0] != 0) {
                endDate = EndDate(endDateArray[0], endDateArray[1], endDateArray[2])
            }

            val settedRate = savedInstanceState.getFloat("settedRate")
            binding.endBookDialogRateBar.rating = settedRate
        }
    }


}


