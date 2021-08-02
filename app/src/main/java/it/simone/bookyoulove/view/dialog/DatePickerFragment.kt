package it.simone.bookyoulove.view.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import it.simone.bookyoulove.view.END_DATE_SETTER
import it.simone.bookyoulove.view.START_DATE_SETTER
import java.util.*

class DatePickerFragment: DialogFragment(), DatePickerDialog.OnDateSetListener {

    private var callMode : Int = -1

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        callMode = arguments?.getInt("caller") ?: -1
        //Toast.makeText(requireContext(), "$callMode", Toast.LENGTH_SHORT).show()

        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)


        val datePickerDialog = DatePickerDialog(requireContext(), this, year, month, day)

        setMinDate(datePickerDialog)
        setMaxDate(datePickerDialog)

        return datePickerDialog
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        //Faccio +1 perché i mesi sono ritornati a partire da indice 0
        val returnKey: String = if (callMode == START_DATE_SETTER) "startDateKey" else "endDateKey"
        val returnBundle : Bundle = bundleOf("day" to dayOfMonth, "month" to month + 1, "year" to year)

        setFragmentResult(returnKey, returnBundle)
    }


    private fun setMinDate(datePickerDialog: DatePickerDialog) {
        /*
            Utilizzo questa funzione per impostare la data minima: evito che venga impostata una data di fine < data inizio.
            Devo utilizzare il -1 perché la startDate è salvata con i mesi a partire da 1, mentre la Calendar li considera a partire da 0
         */
        val calendar = Calendar.getInstance()
        if (callMode == END_DATE_SETTER && arguments?.getInt("minDay", -1) != -1) {
            calendar.set(arguments?.getInt("minYear")!!, arguments?.getInt("minMonth")!! - 1, arguments?.getInt(("minDay"))!!)
            datePickerDialog.datePicker.minDate = calendar.timeInMillis
        }
    }


    private fun setMaxDate(datePickerDialog: DatePickerDialog) {
        val calendar = Calendar.getInstance()
        if (callMode == START_DATE_SETTER && arguments?.getInt("maxDay", -1) != -1) {
            calendar.set(arguments?.getInt("maxYear")!!, arguments?.getInt("maxMonth")!! - 1, arguments?.getInt(("maxDay"))!!)
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        }
    }

}