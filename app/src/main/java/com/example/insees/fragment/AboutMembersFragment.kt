package com.example.insees.Fragments

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.insees.R
import com.example.insees.adapter.ProfessorAdapter
import com.example.insees.databinding.FragmentAboutMembersBinding
import com.example.insees.model.Professor
import com.example.insees.util.CloudinaryConstants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AboutMembersFragment : Fragment() {

    private lateinit var binding: FragmentAboutMembersBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutMembersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        loadImage(
            CloudinaryConstants.INSEES_IMAGE,
            binding.inseesImage,
            "insees.jpg"
        )

        binding.recyclerView.isNestedScrollingEnabled = false
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)

        loadMembers()
    }

    private fun loadImage(
        imageUrl: String,
        imageView: ImageView,
        localFileName: String
    ) {

        val localFile = File(requireContext().filesDir, localFileName)

        if (localFile.exists()) {

            Glide.with(this)
                .load(localFile)
                .placeholder(android.R.color.white)
                .error(R.drawable.err)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)

        } else {

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {

                try {

                    val bitmap = Glide.with(requireContext())
                        .asBitmap()
                        .load(imageUrl)
                        .submit()
                        .get()

                    saveImageToLocalFile(bitmap, localFile)

                    withContext(Dispatchers.Main) {
                        imageView.setImageBitmap(bitmap)
                    }

                } catch (e: Exception) {

                    Log.e("AboutMembers", "Image Load Error", e)

                    withContext(Dispatchers.Main) {
                        imageView.setImageResource(R.drawable.err)
                    }
                }
            }
        }
    }


    private fun loadMembers() {
        db.collection("Professors")
            .get()
            .addOnSuccessListener { documents ->
                val membersList = mutableListOf<Professor>()
                for (doc in documents) {
                    try {
                        val member = doc.toObject(Professor::class.java)
                        membersList.add(member)
                    }
                    catch (e: Exception) {
                        Log.e("FirestoreParseError", "Error parsing doc ${doc.id}", e)
                    }
                }
                if (membersList.isNotEmpty()) {
                    binding.recyclerView.adapter = ProfessorAdapter(membersList) { member ->
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(member.website_url)
                        )
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(
                        context,
                        "No members found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("FirestoreError", "Error loading data", e)
            }
    }

    private fun saveImageToLocalFile(bitmap: Bitmap, file: File) {

        try {

            FileOutputStream(file).use { out ->
                bitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    50,
                    out
                )
            }

        } catch (e: IOException) {

            e.printStackTrace()

            Toast.makeText(
                context,
                "Failed to save image locally",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}