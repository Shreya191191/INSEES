package com.example.insees.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.insees.R
import com.example.insees.adapter.ProfessorAdapter
import com.example.insees.databinding.FragmentAboutMembersBinding
import com.example.insees.model.Professor
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AboutMembersFragment : Fragment() {

    private lateinit var binding: FragmentAboutMembersBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentAboutMembersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val storageRef = FirebaseStorage.getInstance().reference.child("images/Insees.jpg")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            if (isAdded) {
                Glide.with(this)
                    .load(uri)
                    .placeholder(android.R.color.white)
                    .error(R.drawable.err)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.inseesImage)
            }
        }.addOnFailureListener {
            if (isAdded) {
                binding.inseesImage.setImageResource(R.drawable.err)
            }
        }

        binding.recyclerView.isNestedScrollingEnabled = false
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)

        loadMembers()
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
                    } catch (e: Exception) {
                        Log.e("FirestoreParseError", "Error parsing doc ${doc.id}", e)
                    }
                }
                if (membersList.isNotEmpty()) {
                    binding.recyclerView.adapter = ProfessorAdapter(membersList) { member ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(member.website_url))
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(context, "No members found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("FirestoreError", "Error loading data", e)
            }
    }

}