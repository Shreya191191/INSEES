package com.example.insees.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.insees.R
import com.example.insees.adapter.YearAdapter
import com.example.insees.databinding.FragmentYearListBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class YearFragment : Fragment() {

    private lateinit var binding: FragmentYearListBinding
    private lateinit var subjectListView: ListView
    private lateinit var selectedSemester: String
    private lateinit var bottomNavigationView: BottomNavigationView

    private val db = FirebaseFirestore.getInstance()

    // lateinit ki jagah normal String
    private var pdfUrl = ""
    private var driveUrl = ""

    private val options = arrayListOf<String>()

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
    ): View {
        binding = FragmentYearListBinding.inflate(inflater, container, false)
        subjectListView = binding.subjectsList
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListView()
        bottomNavigationView =
            requireActivity().findViewById(R.id.bvNavBar)
        fetchOptions()          // <-- yaha shift kiya hai
        binding.btnSubjectBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("TEST", "Year Resume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("TEST", "Year Pause")
    }

    private fun setupListView() {

        subjectListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                when (position) {
                    0 -> {
                        // baad me PdfViewer navigate karenge
                    }
                    1 -> {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(driveUrl)
                            )
                        )
                    }
                }
            }
    }

    private fun fetchOptions() {

        Log.d("PYQ", "Selected Semester = $selectedSemester")

        db.collection("PYQs")
            .document(selectedSemester)
            .get()
            .addOnSuccessListener { document ->
                // Fragment destroy ho chuka ho to kuch mat karo
                if (!isAdded) return@addOnSuccessListener
                Log.d("PYQ", "Document exists = ${document.exists()}")
                if (!document.exists()) {
                    Toast.makeText(
                        requireContext(),
                        "Document not found",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnSuccessListener
                }

                pdfUrl = document.getString("pdf_url") ?: ""
                driveUrl = document.getString("drive_url") ?: ""

                Log.d("PYQ", "PDF = $pdfUrl")
                Log.d("PYQ", "Drive = $driveUrl")

                options.clear()
                options.add("📄 Latest PYQ")
                options.add("📂 Semester Drive")
                context?.let {
                    subjectListView.adapter =
                        YearAdapter(it, options.toTypedArray())
                }
                Log.d("PYQ", "Adapter Set")
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                Log.e("PYQ", "Firestore Error", it)
                Toast.makeText(
                    requireContext(),
                    it.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}