package com.example.insees.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.insees.R

class YearAdapter(
    context: Context,
    private val options: Array<String>
) : ArrayAdapter<String>(context,
    R.layout.item_year,
    options) {

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        Log.d("PYQ", "getView position = $position")
        val view = convertView
            ?: LayoutInflater.from(context)
                .inflate(R.layout.item_year, parent, false)

        val subjectName =
            view.findViewById<TextView>(R.id.subject_name)
        val icon =
            view.findViewById<ImageView>(R.id.study_material_icon)
        subjectName.text = options[position]

        if (position == 0) {
            icon.setImageResource(R.drawable.baseline_cancel_24)
        } else {
            // Google Drive
            icon.setImageResource(R.drawable.baseline_cancel_24)
        }
        return view
    }
    override fun getCount(): Int {
        Log.d("PYQ", "Count = ${options.size}")
        return options.size
    }
}