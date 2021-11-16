package tokyo.mstp015v.atendance

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DateDialog(private val y : Int,private val m : Int , private val d : Int,private val onSelected:(Int,Int,Int,Int)->Unit) : DialogFragment(), DatePickerDialog.OnDateSetListener{
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return DatePickerDialog(requireActivity(),this,y,m,d)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year,month,dayOfMonth)
        onSelected(year,month,dayOfMonth,calendar.get(Calendar.DAY_OF_WEEK))
    }
}