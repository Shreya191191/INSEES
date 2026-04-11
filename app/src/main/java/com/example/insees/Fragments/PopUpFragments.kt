package com.example.insees.Fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.insees.Utils.DialogAddBtnClickListener
import com.example.insees.databinding.FragmentPopUpBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class PopUpFragment : DialogFragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private lateinit var binding: FragmentPopUpBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var listener: DialogAddBtnClickListener

    fun setListener(listener: DialogAddBtnClickListener) {
        this.listener = listener
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private var day = 0
    private var month = 0
    private var year = 0
    private var hour = 0
    private var minute = 0

    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0
    private var savedHour = 0
    private var savedMinute = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPopUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        registerEvents()
        setCurrentDateTime()
        pickDate()
        pickTime()
    }

    private fun registerEvents() {
        binding.btnPopUpAddTask.setOnClickListener {
            val todoTitle = binding.etPopUpTaskName.text.toString()
            val todoDesc = binding.etPopUpTaskDesc.text.toString()
            val todoTime = binding.etPopUpTaskTime.text.toString()
            val todoDate = binding.etPopUpTaskDate.text.toString()

            if (todoTitle.isNotEmpty() && todoDesc.isNotEmpty() && todoTime.isNotEmpty() && todoDate.isNotEmpty()) {
                listener.onSaveTask(
                    todoTitle,
                    binding.etPopUpTaskName,
                    todoDesc,
                    binding.etPopUpTaskDesc,
                    todoTime,
                    binding.etPopUpTaskTime,
                    todoDate,
                    binding.etPopUpTaskDate
                )
            } else {
                Toast.makeText(context, "Please complete all the required fields", Toast.LENGTH_SHORT).show()
            }
        }
        binding.cancelPopUpButton.setOnClickListener {
            dismiss()
            findNavController().popBackStack()
        }
    }

    private fun init() {
        databaseRef = FirebaseDatabase.getInstance().reference
    }

    private fun getDateTimeCalendar() {
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR_OF_DAY)
        minute = cal.get(Calendar.MINUTE)
    }

    private fun pickDate() {
        binding.etPopUpTaskDate.setOnClickListener {
            getDateTimeCalendar()
            DatePickerDialog(requireContext(), this, year, month, day).show()
        }
    }

    private fun pickTime() {
        binding.etPopUpTaskTime.setOnClickListener {
            getDateTimeCalendar()
            TimePickerDialog(requireContext(), this, hour, minute, true).show()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month + 1
        savedYear = year
        binding.etPopUpTaskDate.text = "$savedDay/$savedMonth/$savedYear"
    }

    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        savedHour = hourOfDay
        savedMinute = if (minute < 10) "0$minute" else minute.toString()
        binding.etPopUpTaskTime.text = "$savedHour:$savedMinute"
    }

    @SuppressLint("SetTextI18n")
    private fun setCurrentDateTime() {
        getDateTimeCalendar()
        binding.etPopUpTaskDate.text = "$day/${month + 1}/$year"
        binding.etPopUpTaskTime.text = "$hour:${if (minute < 10) "0$minute" else minute}"
    }
}