package com.example.insees.fragment

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.insees.util.DOWNLOAD_FAILED
import com.example.insees.util.DOWNLOAD_SUCCESS
import com.example.insees.util.DownloadProgressUpdater
import com.example.insees.databinding.FragmentPdfViewerBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import androidx.core.net.toUri
import androidx.core.content.edit

class PdfViewerFragment : Fragment(), DownloadProgressUpdater.DownloadProgressListener {

    private lateinit var binding:FragmentPdfViewerBinding
    private lateinit var snackbar: Snackbar
    private lateinit var downloadManager: DownloadManager
    private lateinit var downloadUrl: String
    private lateinit var fileName: String
    private lateinit var selectedYear: String
    private lateinit var selectedSemester: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            fileName = it.getString("file_name", "")
            downloadUrl = it.getString("download_url", "")
            selectedYear = it.getString("selected_year", "")
            selectedSemester = it.getString("selected_semester", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPdfViewerBinding.inflate(inflater,container,false)


        downloadManager = requireContext().getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        snackbar = Snackbar.make(binding.PdfViewerLayout, "", Snackbar.LENGTH_INDEFINITE).setTextColor(
            Color.WHITE).setActionTextColor(Color.DKGRAY)

        val downloadDir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),"Insees")

        val file = File(downloadDir,fileName)

        loadPdf()

        binding.floatingActionButton.setOnClickListener {
            downloadPdf(downloadUrl, file)
        }

        return binding.root
    }

    private fun loadPdf(){

        lifecycleScope.launch(Dispatchers.IO) {

            val inputStream = URL(downloadUrl).openStream()
            withContext(Dispatchers.Main) {
                binding.pdfView.fromStream(inputStream).onRender { pages ->
                    if (pages >= 1) {
                        binding.progressBar.visibility = View.GONE
                    }
                }.load()
            }
        }
    }

    private fun downloadPdf(downloadUrl: String?, file: File) {
        try {
            val downloadUri = downloadUrl?.toUri()
            val request = DownloadManager.Request(downloadUri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(file.name)
                .setMimeType("application/pdf")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(file))
            val downloadId = downloadManager.enqueue(request)
            binding.progressBar.visibility = View.VISIBLE
            val downloadProgressHelper = DownloadProgressUpdater(downloadManager, downloadId, this)
            lifecycleScope.launch(Dispatchers.IO) {
                downloadProgressHelper.run()
            }
            snackbar.show()

        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun updateProgress(progress: Long) {
        lifecycleScope.launch(Dispatchers.Main) {
            when (progress) {
                DOWNLOAD_SUCCESS -> {
                    snackbar.setText("Downloading ...... $progress%")
                    binding.progressBar.visibility = View.INVISIBLE
                    Toast.makeText(
                        context,
                        " Downloaded Successfully !!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val sharedPref = requireContext().getSharedPreferences("file_name", Context.MODE_PRIVATE)
                    sharedPref.edit { putString("$selectedSemester $selectedYear", fileName) }

                    snackbar.dismiss()
                }

                DOWNLOAD_FAILED -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    Toast.makeText(
                        context,
                        " Downloaded Failed !!",
                        Toast.LENGTH_SHORT
                    ).show()
                    snackbar.dismiss()
                }
                else -> {
                    binding.progressBar.progress = progress.toInt()
                    snackbar.setText("Downloading ...... $progress%")
                }
            }
        }
    }

}