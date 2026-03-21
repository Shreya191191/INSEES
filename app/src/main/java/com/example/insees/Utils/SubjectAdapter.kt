package com.example.insees.Utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.insees.R
import com.google.android.material.card.MaterialCardView

class SubjectAdapter(context: Context, private val subjects: Array<String>) :
    ArrayAdapter<String>(context, R.layout.semester_subject, subjects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.semester_subject, parent, false)

        val parentTitleTv = view.findViewById<TextView>(R.id.subject_name)

        parentTitleTv.text = subjects[position]

        return view
    }
}