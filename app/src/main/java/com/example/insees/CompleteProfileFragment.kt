package com.example.insees

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.insees.databinding.FragmentCompleteProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CompleteProfileFragment : Fragment() {

    private lateinit var binding: FragmentCompleteProfileBinding
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompleteProfileBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        val db = Firebase.firestore

        initActivityResultLaunchers()

        binding.openCamera.setOnClickListener {
            requestPermissions()
            openCamera()
        }

        binding.uploadImage.setOnClickListener {
            galleryLauncher.launch("image/*")

        }

        binding.btnNextCompleteProfile.setOnClickListener {
            if (validateField()) {
                val name = binding.etNameCompleteProfile.toString()
                val profilePhoto = binding.profilePhoto.toString()
                val user = hashMapOf(
                    "name" to name,
                    "profile_photo" to profilePhoto
                )

                db.collection("users")
                    .add(user)
            }
        }

        return binding.root
    }

    private fun initActivityResultLaunchers() {
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val selectedImageBitmap =
                        result.data?.extras?.get("data") as Bitmap
                    // Handle the captured image bitmap
                    binding.profilePhoto.apply {
                        visibility= View.VISIBLE
                        setImageBitmap(selectedImageBitmap)
                    }
                }
            }


        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                binding.profilePhoto.apply {
                    visibility= View.VISIBLE
                    setImageURI(uri)
                }
            }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            cameraLauncher.launch(takePictureIntent)
        }
    }


    private fun validateField(): Boolean {
        if (binding.etNameCompleteProfile.toString() == "") {
            binding.etNameCompleteProfileLayout.error = "This is required field"
            return false
        }
        return true
    }


    private fun requestPermissions() {
        val permissionToRequest = mutableListOf<String>()

        if (!hasCameraPermissions()) {
            permissionToRequest.add(Manifest.permission.CAMERA)
        }
        if (!hasReadStoragePermissions()) {
            permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!hasWriteStoragePermissions()) {
            permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(permissionToRequest.isNotEmpty()){
            ActivityCompat.requestPermissions(requireContext() as Activity, permissionToRequest.toTypedArray(),0)
        }
    }

    private fun hasCameraPermissions() =
        ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun hasReadStoragePermissions() =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasWriteStoragePermissions() =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty() && requestCode==0){
            for(i in grantResults.indices){
                if(grantResults[i]== PackageManager.PERMISSION_GRANTED){
                    Log.d("permission", "${permissions[i]} granted")
                }
            }
        }
    }
}