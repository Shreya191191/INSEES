package com.example.insees.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.insees.adapter.YearAdapter
import com.example.insees.R
import com.example.insees.util.FirebaseManager
import com.example.insees.databinding.FragmentYearListBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class YearFragment : Fragment(){

    private lateinit var binding:FragmentYearListBinding
    private lateinit var subjectListView: ListView
    private lateinit var selectedSemester: String
    private lateinit var storageRef: StorageReference
    private lateinit var downloadUrl: String
    private lateinit var bottomNavigationView : BottomNavigationView
    private var years: MutableList<String> = mutableListOf()
    private var cacheYears = ""

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
        binding = FragmentYearListBinding.inflate(inflater,container,false)
        subjectListView = binding.subjectsList
        storageRef = FirebaseManager.getFirebaseStorage().reference.child("PYQs").child(selectedSemester)
        fetchDataAndSetupAdapter()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListView()
        bottomNavigationView = requireActivity().findViewById(R.id.bvNavBar)

        binding.btnSubjectBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNavigationView.visibility = View.GONE
        fetchDataAndSetupAdapter()
    }


    private fun setupListView() {
        subjectListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedYear = years[position]

            getDataFromFirebase(selectedYear, selectedSemester)
        }
    }

    private fun fetchDataAndSetupAdapter() {
        lifecycleScope.launch {

            val sharedPrefYears = requireContext().getSharedPreferences("years", Context.MODE_PRIVATE)
            val cacheYear = sharedPrefYears.getString(selectedSemester,null)


            if(cacheYear != null){

                years = cacheYear.substring(1).split("\\s+".toRegex()).toMutableList()

            }else {
                years = getYears()
                sharedPrefYears.edit().putString(selectedSemester, cacheYears).apply()
            }

            val adapter = context?.let { YearAdapter(it, years.toTypedArray()) }
            subjectListView.adapter = adapter

            updateData(sharedPrefYears)
        }
    }


    private suspend fun getYears(): MutableList<String> {
        return suspendCoroutine { continuation ->
            storageRef.listAll().addOnSuccessListener { result ->
                val years = mutableListOf<String>()

                cacheYears = ""
                for (prefix in result.prefixes) {
                    years.add(prefix.name)
                    cacheYears = "$cacheYears ${prefix.name}"
                }
                continuation.resume(years)

            }.addOnFailureListener { exception ->
                Toast.makeText(context, "Something went wrong: ${exception.message}", Toast.LENGTH_SHORT).show()
                continuation.resumeWithException(exception)
            }
        }
    }

    private suspend fun updateData(sharedPrefYears: SharedPreferences) {
        lifecycleScope.launch {
            val years = getYears()
            val updatedYears = years.joinToString(separator = " ")


            val cacheYears = sharedPrefYears.getString(selectedSemester,null)

            if (cacheYears != "") {
                if (updatedYears != cacheYears?.substring(1)){
                    sharedPrefYears.edit().putString(selectedSemester, " $updatedYears").apply()
                }
            }
        }
    }


    private fun getDataFromFirebase(selectedYear:String, selectedSemester: String ) {

        storageRef = storageRef.child(selectedYear)

        val sharedPref = requireContext().getSharedPreferences("file_name", Context.MODE_PRIVATE)

        var fileName= sharedPref.getString("$selectedSemester $selectedYear",null)

        val downloadDir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),"Insees")

        if(!downloadDir.exists()){
            downloadDir.mkdirs()
        }

        if(fileName!=null) {
            val file = File(downloadDir, fileName)

            if (file.exists()) {
                openPdf(file)
            }
        }else{
            storageRef.listAll()
                .addOnSuccessListener { listResult ->

                    listResult.items.firstOrNull()?.let { fileRef ->
                        fileName = fileRef.name

                        binding.progressBar.visibility = View.VISIBLE
                        fileRef.downloadUrl.addOnCompleteListener {
                            if (it.isSuccessful ) {
                                downloadUrl = it.result.toString()
                                if (isAdded) {
                                    findNavController().navigate(
                                        R.id.action_yearFragment_to_pdfViewerFragment,
                                        Bundle().apply {
                                            putString("file_name", fileName)
                                            putString("download_url", downloadUrl)
                                            putString("selected_semester", selectedSemester)
                                            putString("selected_year", selectedYear)
                                        })
                                    binding.progressBar.visibility = View.GONE
                                }

                            }
                        }
                    }?.addOnFailureListener { exception ->

                        Toast.makeText(
                            context,
                            "Error getting file: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun openPdf(file:File){
        val path = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(path,"application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(intent)
    }


}