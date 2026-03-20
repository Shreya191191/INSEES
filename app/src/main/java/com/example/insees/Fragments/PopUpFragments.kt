package com.example.insees.Fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.example.insees.R
import com.example.insees.databinding.FragmentPopUpBinding
import java.util.Calendar


class PopUpFragment : DialogFragment(),DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener {

    private lateinit var binding:FragmentPopUpBinding

    var day=0
    var month=0
    var year=0
    var hour=0
    var minute=0

    var savedDay=0
    var savedMonth=0
    var savedYear=0
    var savedHour=0
    var savedMinute=0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentPopUpBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pickDate()
        pickTime()
        binding.cancelPopUpButton.setOnClickListener {
            dismiss()
        }
    }

    private fun getDateTimeCalendar(){

        val cal=Calendar.getInstance()
        day=cal.get(Calendar.DAY_OF_MONTH)
        month=cal.get(Calendar.MONTH)
        year=cal.get(Calendar.YEAR)
        hour=cal.get(Calendar.HOUR)
        minute=cal.get(Calendar.MINUTE)

    }

    private fun pickDate(){
        binding.etPopUpTaskDate.setOnClickListener {
            getDateTimeCalendar()
            DatePickerDialog(requireContext(),this,year,month,day).show()
        }
    }

    private fun pickTime() {
        binding.etPopUpTaskTime.setOnClickListener {
            getDateTimeCalendar()
            TimePickerDialog(requireContext(),this,hour,minute,true).show()
        }
    }
    @SuppressLint("SetTextI18n")
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {

        savedDay=dayOfMonth
        savedMonth=month
        savedYear=year

        binding.etPopUpTaskDate.text="$savedDay/$savedMonth/$savedYear"
    }

    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {

        savedHour=hourOfDay
        savedMinute=minute

        binding.etPopUpTaskTime.text="$savedHour:$savedMinute"

    }

}