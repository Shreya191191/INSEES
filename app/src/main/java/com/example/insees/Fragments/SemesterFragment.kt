package com.example.insees.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.navigation.fragment.findNavController
import com.example.insees.R
import com.example.insees.Utils.SemesterAdapter
import com.example.insees.databinding.FragmentSemesterBinding

class SemesterFragment : Fragment() {

    private lateinit var semesterListView: ListView
    private lateinit var binding:FragmentSemesterBinding
    private val semesters = arrayOf("Semester 1", "Semester 2", "Semester 3", "Semester 4")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSemesterBinding.inflate(inflater,container,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        semesterListView = binding.SemesterList
        val adapter = SemesterAdapter(requireContext(), semesters)
        semesterListView.adapter = adapter
        semesterListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedSemester = semesters[position]
                findNavController().navigate(R.id.action_semesterFragment_to_subjectsFragment, Bundle().apply {
                    putString("selected_semester", selectedSemester)
                })
            }

    }
}