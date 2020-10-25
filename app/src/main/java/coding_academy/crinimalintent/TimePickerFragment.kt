package coding_academy.crinimalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.sql.Time
import java.text.DateFormat
import java.util.*
private const val DIALOG_TIME  = "Dialogtime"

class TimePickerFragment: DialogFragment() {
    interface  Callbacks {
        fun onTimeSelected(time: Date)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val time1 = arguments?.getSerializable(DIALOG_TIME) as Date
        val calendar = Calendar.getInstance()
        calendar.time = time1
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val time = TimePickerDialog(
            requireContext(),
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                val calendar1 = Calendar.getInstance()
                calendar1.time = time1
                calendar1.set(Calendar.SECOND, 0)
                calendar1.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar1.set(Calendar.MINUTE, minute)
                val resultTime = calendar1.time as Date
                targetFragment?.let { fragment ->
                    (fragment as Callbacks).onTimeSelected(resultTime)
                }
            }, hour, minute, false)

        return time
    }

    companion object {
        fun newInstance(time:Date): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(DIALOG_TIME, time)
            }
            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }


}



