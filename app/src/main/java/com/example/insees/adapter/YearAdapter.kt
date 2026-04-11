package com.example.insees.adapter
import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.insees.R
import java.io.File


class YearAdapter(context: Context, private val year: Array<String>,private val selectedSemester: String) :
    ArrayAdapter<String>(context, R.layout.item_year, year) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_year, parent, false)

        val subjectName = view.findViewById<TextView>(R.id.subject_name)
        val icon = view.findViewById<ImageView>(R.id.study_material_icon)

        subjectName.text = year[position]

        val sharedPref = context.getSharedPreferences("file_name", Context.MODE_PRIVATE)
        val fileName = sharedPref.getString("$selectedSemester ${year[position]}", null)

        val downloadDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Insees")
        val file = if (!fileName.isNullOrEmpty()) File(downloadDir, fileName) else null

        if (file != null && file.exists()) {
            icon.setImageResource(R.drawable.baseline_check_circle_24) // downloaded icon
        } else {
            icon.setImageResource(R.drawable.baseline_file_download_24) // download icon
        }

        return view
    }

}