package com.example.insees.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.insees.util.FirebaseManager
import com.example.insees.databinding.FragmentCompleteProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.ref.WeakReference

class CompleteProfileFragment : Fragment() {


    private lateinit var binding: FragmentCompleteProfileBinding
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var database: FirebaseDatabase
    private var profilePhoto: String = ""
    private lateinit var result: Bitmap
    private lateinit var email: String
    private lateinit var password: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = view.findNavController()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.coroutineContext.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompleteProfileBinding.inflate(inflater, container, false)
        auth = FirebaseManager.getFirebaseAuth()
        database = FirebaseManager.getFirebaseDatabase()

        binding.progressBar.visibility = View.GONE
        binding.scrollViewCompleteProfile.visibility = View.VISIBLE

        requestPermissions()

        email = requireArguments().getString("email")!!
        password = requireArguments().getString("password")!!

        initActivityResultLaunchers()

        binding.openCamera.setOnClickListener {
            openCamera()
        }

        binding.uploadImage.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        //ankitk_ug_22@ei.nits.ac.in
        //123456

        binding.btnNextCompleteProfile.setOnClickListener {
            binding.btnNextCompleteProfile.isEnabled = false

            binding.progressBar.visibility = View.VISIBLE
            binding.scrollViewCompleteProfile.isClickable = false
            binding.scrollViewCompleteProfile.isFocusable = false
            lifecycleScope.launch {
                signUp(email, password)
                binding.progressBar.visibility = View.GONE
                binding.scrollViewCompleteProfile.isClickable = true
                binding.scrollViewCompleteProfile.isFocusable = true
            }

            binding.btnNextCompleteProfile.isEnabled = true
        }

        binding.closeImage.setOnClickListener {
            profilePhoto = ""
            binding.profilePhoto.visibility = View.INVISIBLE
            binding.profileLayout.visibility = View.VISIBLE
            binding.closeImage.visibility = View.INVISIBLE
        }

        return binding.root
    }

    private suspend fun signUp(email: String, password: String) {
        if (!::result.isInitialized) {
            Toast.makeText(context, "Please upload a profile photo", Toast.LENGTH_SHORT).show()
            return
        }
        if (!validateField()) {
            Toast.makeText(context, "Enter Your Name", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            auth.createUserWithEmailAndPassword(email, password).await()

            // Wait for image upload
            saveImage(this.result) // make this a suspend function too

            // Then send verification link
            sendLink()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun sendLink() {
        try {
            auth.currentUser?.sendEmailVerification()?.await()
            Toast.makeText(requireContext(), "Please verify your email", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to send verification email: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initActivityResultLaunchers() {
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
                if (res.resultCode == Activity.RESULT_OK) {
                    val selectedImageBitmap =
                        res.data?.extras?.get("data") as Bitmap
                    result = selectedImageBitmap
                    binding.closeImage.visibility = View.VISIBLE

                    binding.profileLayout.visibility = View.INVISIBLE

                    binding.profilePhoto.apply {
                        visibility = View.VISIBLE

                        val result = WeakReference(
                            selectedImageBitmap.scale(
                                selectedImageBitmap.height,
                                selectedImageBitmap.width,
                                false
                            ).copy(
                                Bitmap.Config.RGB_565, true
                            )
                        ).get()

                        setImageBitmap(result)

                    }
                }
            }


        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) {
                    try {
                        val inputStream = requireContext().contentResolver.openInputStream(uri)
                        result = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()

                        binding.profileLayout.visibility = View.INVISIBLE
                        binding.profilePhoto.apply {
                            visibility = View.VISIBLE
                            setImageBitmap(result)
                        }

                        binding.closeImage.visibility = View.VISIBLE

                    } catch (e: IOException) {
                        Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            cameraLauncher.launch(takePictureIntent)
        }
    }


    private fun validateField(): Boolean {
        if (binding.etNameCompleteProfile.editableText.toString().trim() == "") {
            Toast.makeText(context, "This is required field", Toast.LENGTH_SHORT).show()
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
        if (permissionToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireContext() as Activity,
                permissionToRequest.toTypedArray(),
                0
            )
        }
    }

    private fun hasCameraPermissions() =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

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

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && requestCode == 0) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("permission", "${permissions[i]} granted")
                }
            }
        }
    }

//    private fun saveImage(image:Bitmap,context:Context ): Uri {
//
//        val imagesFolder = File(context.cacheDir, "images")
//        var uri: Uri
//        try{
//            imagesFolder.mkdir()
//            val file = File(imagesFolder, "captured_image.jpg")
//            val stream = FileOutputStream(file)
//            image.compress(Bitmap.CompressFormat.JPEG , 100, stream)
//            stream.flush()
//            stream.close()
//            uri = FileProvider.getUriForFile(context.applicationContext, "com.example.insees"+".provider", file)
//        }
//        catch (e:FileNotFoundException){
//            e.printStackTrace()
//        }catch (e: IOException){
//            e.printStackTrace()
//        }
//        return uri
//    }

    private suspend fun saveImage(bitmap: Bitmap){
        val uid = auth.currentUser?.uid ?: return
        val storageRef = FirebaseManager.getFirebaseStorage().reference
        val imageRef = storageRef.child("Profile/$uid.jpg")

        // Convert bitmap to byte array
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos)
        val data = baos.toByteArray()

        imageRef.putBytes(data).await()
        val uri = imageRef.downloadUrl.await() // Get download URL
        val name = binding.etNameCompleteProfile.getText().toString().trim()
        val user = mapOf("name" to name, "profilePhoto" to uri.toString())
        database.getReference("users").child(uid).updateChildren(user).await()
        Log.d("UID", "Hello")
    }
}