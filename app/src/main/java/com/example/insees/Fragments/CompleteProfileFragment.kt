package com.example.insees.Fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
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
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.insees.Utils.FirebaseManager
import com.example.insees.databinding.FragmentCompleteProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompleteProfileBinding.inflate(inflater, container, false)
        auth = FirebaseManager.getFirebaseAuth()
        database = FirebaseManager.getFirebaseDatabase()

        requestPermissions()

        val email = requireArguments().getString("email")!!
        val password = requireArguments().getString("password")!!

        initActivityResultLaunchers()

        binding.openCamera.setOnClickListener {
            openCamera()
        }

        binding.uploadImage.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.btnNextCompleteProfile.setOnClickListener {
            signUp(email, password)
        }

        binding.closeImage.setOnClickListener{
            profilePhoto = ""
            binding.profilePhoto.visibility = View.INVISIBLE
            binding.profileLayout.visibility = View.VISIBLE
            binding.closeImage.visibility  = View.INVISIBLE
        }

        return binding.root
    }

    private fun signUp(email: String, password:String){
        if(validateField()) {
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{task->
                if(task.isSuccessful){

                    val name = binding.etNameCompleteProfile.text.toString()

                    val uid = auth.currentUser?.uid.toString()

                    val user = hashMapOf(
                        "name" to name,
                        "profile_photo" to profilePhoto
                    )

                    val databaseRef = database.getReference("users")
                    databaseRef.child(uid).setValue(user)

                    auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                        Toast.makeText(requireContext(), "Please Verify Your Email", Toast.LENGTH_SHORT).show()
                        navController.navigateUp()
                    }
                        ?.addOnFailureListener {
                            Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
                        }
//                    val intent = Intent(context, HomeActivity::class.java)
//                    startActivity(intent)
//                    navController.navigate(R.id.action_completeProfileFragment_to_loginFragment)
                }

            }.addOnFailureListener { exception->
                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.nav_host_fragment, LoginFragment())
//                .commit()
                navController.navigateUp()
            }
        }
    }

    private fun initActivityResultLaunchers() {
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val selectedImageBitmap =
                        result.data?.extras?.get("data") as Bitmap

                    binding.closeImage.visibility = View.VISIBLE

                    binding.profileLayout.visibility = View.INVISIBLE

                    // Handle the captured image bitmap
                    binding.profilePhoto.apply {
                        visibility= View.VISIBLE

                        val result  = WeakReference(Bitmap.createScaledBitmap(selectedImageBitmap,
                            selectedImageBitmap.height, selectedImageBitmap.width, false).copy(
                            Bitmap.Config.RGB_565, true
                        )).get()

                        setImageBitmap(result)

                        val imageUri = result?.let { saveImage(it, context) }

                        profilePhoto = imageUri.toString()
                    }
                }
            }


        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if(uri != null){
                    binding.profileLayout.visibility = View.INVISIBLE
                    binding.profilePhoto.apply {
                        visibility = View.VISIBLE
                        setImageURI(uri)
                        binding.closeImage.visibility = View.VISIBLE
                        profilePhoto = uri.toString()
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

    @Deprecated("Deprecated in Java")
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

    private fun saveImage(image:Bitmap,context:Context ): Uri {

        val imagesFolder = File(context.cacheDir, "images")
        lateinit var uri: Uri
        try{
            imagesFolder.mkdir()
            val file = File(imagesFolder, "captured_image.jpg")
            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.JPEG , 100, stream)
            stream.flush()
            stream.close()
            uri = FileProvider.getUriForFile(context.applicationContext, "com.example.insees"+".provider", file)
        }
        catch (e:FileNotFoundException){
            e.printStackTrace()
        }catch (e: IOException){
            e.printStackTrace()
        }
        return uri
    }

}