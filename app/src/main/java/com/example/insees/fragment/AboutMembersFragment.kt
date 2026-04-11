package com.example.insees.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.insees.R
import com.example.insees.adapter.ProfessorAdapter
import com.example.insees.databinding.FragmentAboutMembersBinding
import com.example.insees.model.Professor
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AboutMembersFragment : Fragment() {

    private lateinit var binding: FragmentAboutMembersBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentAboutMembersBinding.inflate(inflater, container, false)
        loadViews()
        return binding.root
    }

    private fun loadViews() {
        val storageRef = FirebaseStorage.getInstance().reference.child("images/Insees.jpg")
        val localFile = File(requireContext().filesDir, "inseesImage")

        if (localFile.exists()){
            Glide.with(this)
                .load(localFile)
                .placeholder(android.R.color.white) // Use a placeholder image
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Enable disk caching
                .into(binding.inseesImage)
        }
        else {
            lifecycleScope.launch(Dispatchers.Default){
                val uri = storageRef.downloadUrl.await()
                if (isAdded) {
                    Glide.with(this@AboutMembersFragment)
                        .asBitmap()
                        .load(uri)
                        .placeholder(android.R.color.white)
                        .error(R.drawable.err)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                binding.inseesImage.setImageBitmap(resource)
                                saveImageToLocalFile(resource, localFile)
                            }
                            override fun onLoadCleared(placeholder: Drawable?) {

                            }
                        })
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.isNestedScrollingEnabled = false
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)

        lifecycleScope.launch{
            loadMembers()
        }
    }

    private suspend fun loadMembers() {
        try {
            val snapshot = db.collection("Professors").get().await()
            val membersList = snapshot.mapNotNull { doc ->
                try {
                    doc.toObject(Professor::class.java)
                } catch (e: Exception) {
                    Log.e("FirestoreParseError", "Error parsing doc ${doc.id}", e)
                    null
                }
            }

            if (membersList.isNotEmpty()) {
                binding.recyclerView.adapter = ProfessorAdapter(membersList) { member ->
                    val intent = Intent(Intent.ACTION_VIEW, member.website_url.toUri())
                    startActivity(intent)
                }
            } else {
                Toast.makeText(context, "No members found", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("FirestoreError", "Error loading data", e)
        }
    }

    private fun saveImageToLocalFile(bitmap: Bitmap, file: File) {
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save image locally", Toast.LENGTH_SHORT).show()
        }
    }

}