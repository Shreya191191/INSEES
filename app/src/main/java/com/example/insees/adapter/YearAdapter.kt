package com.example.insees.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.insees.R



class YearAdapter(context: Context, private val year: Array<String>) :
    ArrayAdapter<String>(context, R.layout.semester_subject, year) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.semester_subject, parent, false)

        val parentTitleTv = view.findViewById<TextView>(R.id.subject_name)

        parentTitleTv.text = year[position]

        return view
    }
}