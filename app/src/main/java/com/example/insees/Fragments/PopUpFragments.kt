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
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.insees.R
import com.example.insees.databinding.FragmentPopUpBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class PopUpFragment : DialogFragment(),DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener {

    private lateinit var binding:FragmentPopUpBinding
    private lateinit var databaseRef:DatabaseReference
    private lateinit var listener:DialogAddBtnClickListener

    fun setListener(listener:DialogAddBtnClickListener){
        this.listener=listener
    }

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

        init(view)
        registerEvents()

        pickDate()
        pickTime()

    }

    private fun registerEvents() {
        binding.btnPopUpAddTask.setOnClickListener {
            val todoTitle=binding.etPopUpTaskName.text.toString()
            val todoDesc=binding.etPopUpTaskDesc.text.toString()
            val todoTime=binding.etPopUpTaskTime.text.toString()
            val todoDate=binding.etPopUpTaskDate.text.toString()

            if (todoTitle.isNotEmpty() && todoDesc.isNotEmpty() && todoTime.isNotEmpty() && todoDate.isNotEmpty()){
                listener.onSaveTask(todoTitle,binding.etPopUpTaskName,todoDesc,binding.etPopUpTaskDesc,todoTime,binding.etPopUpTaskTime,todoDate,binding.etPopUpTaskDate)
            }else{
                Toast.makeText(context,"Please complete all the required fields",Toast.LENGTH_SHORT).show()
            }

        }
        binding.cancelPopUpButton.setOnClickListener {
            dismiss()
        }

    }

    interface DialogAddBtnClickListener {
        fun onSaveTask(
            todoTitle: String,
            todoTitleEt: EditText,
            todoDesc: String,
            todoDescEt: EditText,
            todoTime: String,
            todoTimeEt: TextView,
            todoDate: String,
            todoDateEt: TextView
        )
    }

    private fun init(view: View) {

        databaseRef=FirebaseDatabase.getInstance().reference

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
        savedMonth=month+1
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