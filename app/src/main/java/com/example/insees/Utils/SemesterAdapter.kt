package com.example.insees.Utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.insees.R
import com.google.android.material.card.MaterialCardView

class SemesterAdapter(context: Context, private val semesters: Array<String>) :
    ArrayAdapter<String>(context, R.layout.semester_item_layout, semesters) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.semester_item_layout, parent, false)

        val parentTitleTv = view.findViewById<TextView>(R.id.parentTitleTv)

        parentTitleTv.text = semesters[position]

        return view
    }
}