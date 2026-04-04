package com.example.insees.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.insees.databinding.FragmentAboutMembersBinding
import com.google.firebase.storage.FirebaseStorage

class AboutMembersFragment : Fragment() {
    lateinit var binding: FragmentAboutMembersBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentAboutMembersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getImages()
    }

    private fun getImages() {
        loadImage("inseesimages/president.png", binding.imgPresident)
        loadImage("inseesimages/snehagupta.png", binding.imgVicePresident)
        loadImage("inseesimages/ankitraj.png", binding.imgGenSecretary)
        loadImage("inseesimages/pranjeet.png", binding.imgPranjeet)
        loadImage("inseesimages/hritikaroy.png", binding.imgHritika)
        loadImage("inseesimages/rupantar.png", binding.imgRupantar)
        loadImage("inseesimages/riya.png", binding.imgRiya)
        loadImage("inseesimages/khyanmoi.png", binding.imgKhyanmoi)
        loadImage("inseesimages/bhawna.png", binding.imgBhawna)
        loadImage("inseesimages/sayanrup.png", binding.imgSayanrup)
        loadImage("inseesimages/ankit.png", binding.imgAnkit)
        loadImage("inseesimages/siddharth.png", binding.imgSiddharth)
        loadImage("inseesimages/anuj.png", binding.imgAnuj)
        loadImage("inseesimages/shweta.png", binding.imgShweta)
        loadImage("inseesimages/akash.png", binding.imgAkash)
        loadImage("inseesimages/ankur.png", binding.imgAnkur)
        loadImage("inseesimages/harish.png", binding.imgHarish)
        loadImage("inseesimages/amipsa.png", binding.imgAmipsa)
        loadImage("inseesimages/silpangana.png", binding.imgSilpangana)
    }

    private fun loadImage(remotePath: String, imageView: ImageView) {
        val storageRef = FirebaseStorage.getInstance().reference.child(remotePath)
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            if (isAdded) { // Check if fragment is added to its activity
                Glide.with(this)
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)  // Enable disk caching
                    .into(imageView)
            }
        }.addOnFailureListener {
            if (isAdded) { // Check if fragment is added to its activity
                Toast.makeText(context, "Failed to load image: $remotePath", Toast.LENGTH_SHORT).show()
            }
        }
    }
}