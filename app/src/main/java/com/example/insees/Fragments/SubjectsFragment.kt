package com.example.insees.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.insees.R
import com.example.insees.Utils.SubjectAdapter

class SubjectsFragment : Fragment() {

    private lateinit var subjectListView: ListView
    private lateinit var selectedSemester: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedSemester = it.getString("selected_semester", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_subject_list, container, false)
        subjectListView = view.findViewById(R.id.subjectsList)

        // Here you can populate the subjects based on the selected semester
        val subjects = when (selectedSemester) {
            "Semester 1" -> arrayOf("Subject A", "Subject B", "Subject C")
            "Semester 2" -> arrayOf("Subject D", "Subject E", "Subject F")
            "Semester 3" -> arrayOf("Subject G", "Subject H", "Subject I")
            "Semester 4" -> arrayOf("Subject J", "Subject K", "Subject L")
            "Semester 5" -> arrayOf("Subject M", "Subject N", "Subject L")
            "Semester 6" -> arrayOf("Subject J", "Subject K", "Subject L")
            "Semester 7" -> arrayOf("Subject J", "Subject K", "Subject L")
            "Semester 8" -> arrayOf("Subject J", "Subject K", "Subject L")
            else -> arrayOf()

        }


        val adapter = SubjectAdapter(requireContext(), subjects)
        subjectListView.adapter = adapter

        val backbtn = view.findViewById<ImageView>(R.id.btnSubjectBack)
        backbtn.setOnClickListener {
            findNavController().navigate(R.id.action_subjectsFragment_to_semesterFragment)
        }
        return view
    }
}