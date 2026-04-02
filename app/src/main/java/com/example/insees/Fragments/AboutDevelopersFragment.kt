package com.example.insees.Fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.insees.BottomSheetDialogDevelopers.AishwaryaFragment
import com.example.insees.BottomSheetDialogDevelopers.ShreyaFragment
import com.example.insees.databinding.FragmentAboutDevelopersBinding
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class AboutDevelopersFragment : Fragment() {

    private lateinit var binding: FragmentAboutDevelopersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAboutDevelopersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getImages()

        val bottomSheetAishwarya = AishwaryaFragment()
        val bottomSheetShreya = ShreyaFragment()

        binding.btnSudip.setOnClickListener {
            bottomSheetAishwarya.show(childFragmentManager, "BottomSheetDialog")
        }

        binding.btnAnkit.setOnClickListener {
            bottomSheetShreya.show(childFragmentManager, "BottomSheetDialog")
        }

    }

    private fun getImages() {
        loadImage("images/sudip.jpg", "sudip.jpg") { bitmap ->
            binding.sudipImage.setImageBitmap(bitmap)
        }
        loadImage("images/ankit.jpg", "ankit.jpg") { bitmap ->
            binding.ankitImage.setImageBitmap(bitmap)
        }
        loadImage("images/rishi.jpg", "rishi.jpg") { bitmap ->
            binding.rishiImage.setImageBitmap(bitmap)
        }
        loadImage("images/bishal.jpg", "bishal.jpg") { bitmap ->
            binding.bishalImage.setImageBitmap(bitmap)
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