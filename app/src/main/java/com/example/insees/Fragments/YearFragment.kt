package com.example.insees.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
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
import com.example.insees.Adapters.YearAdapter
import com.example.insees.R
import com.example.insees.Utils.FirebaseManager
import com.example.insees.databinding.FragmentYearListBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.cancel
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
    private var years: MutableList<String> = mutableListOf()

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

        fetchDataAndSetupAdapter()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListView()
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bvNavBar)
        bottomNavigationView.visibility = View.GONE

        binding.btnSubjectBack.setOnClickListener {
            findNavController().navigateUp()
            bottomNavigationView.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        fetchDataAndSetupAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.coroutineContext.cancel()
    }

    private fun setupListView() {
        subjectListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedYear = years[position]
            getDataFromFirebase(selectedYear, selectedSemester)
        }
    }

    private fun fetchDataAndSetupAdapter() {
        lifecycleScope.launch {
            years = getYears(selectedSemester)

            val adapter = YearAdapter(requireContext(), years.toTypedArray())
            subjectListView.adapter = adapter
        }
    }


    private suspend fun getYears(selectedSemester: String): MutableList<String> {
        return suspendCoroutine { continuation ->
            storageRef = FirebaseManager.getFirebaseStorage().reference.child("PYQs").child(selectedSemester)

            storageRef.listAll().addOnSuccessListener { result ->
                val years = mutableListOf<String>()
                for (prefix in result.prefixes) {
                    years.add(prefix.name)
                }
                continuation.resume(years)
            }.addOnFailureListener { exception ->
                Toast.makeText(context, "Something went wrong: ${exception.message}", Toast.LENGTH_SHORT).show()
                continuation.resumeWithException(exception)
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
            Log.d("abcde",fileName)

            if (file.exists()) {
                openPdf(file)
            }
        }else{
            storageRef.listAll()
                .addOnSuccessListener { listResult ->
                    Log.d("abcdefg",selectedYear)
                    listResult.items.firstOrNull()?.let { fileRef ->
                        fileName = fileRef.name
                        Log.d("abcdef", fileName!!)

                        binding.progressBar.visibility = View.VISIBLE
                        fileRef.downloadUrl.addOnCompleteListener {
                            if (it.isSuccessful) {
                                downloadUrl = it.result.toString()
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
                    }?.addOnFailureListener { exception ->
                        Log.d("abcdefgh", selectedYear)
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