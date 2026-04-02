package com.example.insees.Fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.insees.databinding.FragmentAboutMembersBinding
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class AboutMembersFragment : Fragment() {
    lateinit var binding: FragmentAboutMembersBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentAboutMembersBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getImages()
    }

    private fun getImages() {
        loadImage("inseesimages/president.png", "president.png") { bitmap ->
            binding.imgPresident.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/snehagupta.png", "snehagupta.png") { bitmap ->
            binding.imgVicePresident.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/ankitraj.png", "ankitraj.png") { bitmap ->
            binding.imgGenSecretary.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/pranjeet.png", "pranjeet.png") { bitmap ->
            binding.imgPranjeet.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/hritikaroy.png", "hritikaroy.png") { bitmap ->
            binding.imgHritika.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/rupantar.png", "rupantar.png") { bitmap ->
            binding.imgRupantar.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/riya.png", "riya.png") { bitmap ->
            binding.imgRiya.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/khyanmoi.png", "khyanmoi.png") { bitmap ->
            binding.imgKhyanmoi.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/bhawna.png", "bhawna.png") { bitmap ->
            binding.imgBhawna.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/sayanrup.png", "sayanrup.png") { bitmap ->
            binding.imgSayanrup.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/ankit.png", "ankit.png") { bitmap ->
            binding.imgAnkit.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/siddharth.png", "siddharth.png") { bitmap ->
            binding.imgSiddharth.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/anuj.png", "anuj.png") { bitmap ->
            binding.imgAnuj.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/shweta.png", "shweta.png") { bitmap ->
            binding.imgShweta.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/akash.png", "akash.png") { bitmap ->
            binding.imgAkash.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/ankur.png", "ankur.png") { bitmap ->
            binding.imgAnkur.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/harish.png", "harish.png") { bitmap ->
            binding.imgHarish.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/amipsa.png", "amipsa.png") { bitmap ->
            binding.imgAmipsa.setImageBitmap(bitmap)
        }
        loadImage("inseesimages/silpangana.png", "silpangana.png") { bitmap ->
            binding.imgSilpangana.setImageBitmap(bitmap)
        }
    }

    private fun loadImage(remotePath: String, localFileName: String, onImageLoaded: (bitmap: Bitmap) -> Unit) {
        val localFile = File(requireContext().cacheDir, localFileName)

        if (localFile.exists()) {
            // Load the image from the local cache
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            onImageLoaded(bitmap)
        } else {
            // Download the image from Firebase Storage and save it locally
            val storageRef = FirebaseStorage.getInstance().reference.child(remotePath)

            storageRef.getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                onImageLoaded(bitmap)
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to load image: $remotePath", Toast.LENGTH_SHORT).show()
            }
        }
    }
}