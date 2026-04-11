package com.example.insees.util

import android.widget.EditText
import android.widget.TextView

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