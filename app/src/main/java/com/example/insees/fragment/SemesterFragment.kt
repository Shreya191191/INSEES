package com.example.insees.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.insees.adapter.SemesterAdapter
import com.example.insees.R
import com.example.insees.databinding.FragmentSemesterBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.net.toUri

class SemesterFragment : Fragment() {

    private lateinit var semesterListView: ListView
    private lateinit var binding:FragmentSemesterBinding
    //    private lateinit var viewPager: ViewPager2
    private lateinit var navController: NavController
    private val semesters = arrayOf("Semester 1", "Semester 2", "Semester 3", "Semester 4","Semester 5", "Semester 6", "Semester 7", "Semester 8")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSemesterBinding.inflate(inflater,container,false)
        navController = findNavController()
//        viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<BottomNavigationView>(R.id.bvNavBar).visibility = View.VISIBLE
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val drive = Intent(Intent.ACTION_VIEW).apply {
            data =
                "https://drive.google.com/drive/folders/1w4v7vOivziK7RlLT8tOE0jWsQn1ZbyDx".toUri()
        }
        semesterListView = binding.SemesterList
        val adapter = SemesterAdapter(requireContext(), semesters)

        semesterListView.adapter = adapter

        semesterListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedSemester = semesters[position]

                if (selectedSemester == "Semester 1"|| selectedSemester == "Semester 2"){
                    startActivity(drive)
                }else {
                    navController.navigate(
                        R.id.action_viewPagerFragment_to_yearFragment,
                        Bundle().apply {
                            putString("selected_semester", selectedSemester)
                        })
                }
            }

//        val callback = object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                viewPager.currentItem = 0
//            }
//        }
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
}