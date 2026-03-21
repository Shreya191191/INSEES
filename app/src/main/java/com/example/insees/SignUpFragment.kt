package com.example.insees

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.insees.databinding.FragmentSignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater,container,false)


        binding.btnNextSignUp.setOnClickListener {
            if (checkAllFields()) {
                navController.navigate(R.id.action_signUpFragment_to_completeProfileFragment, Bundle().apply {
                    putString("email", binding.etEmailSignup.text.toString())
                    putString("password", binding.etPasswordSignup.text.toString())
                })
            }
        }

        binding.tvLogin.setOnClickListener{
            navController.navigate(R.id.action_signUpFragment_to_loginFragment)
        }

        return binding.root
    }

    private fun checkAllFields(): Boolean{
        val email = binding.etEmailSignup.text.toString()
        if (email == ""){
            binding.etEmailSignupLayout.error = "This is required field"
            return false
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmailSignupLayout.error = "Please enter a valid Email"
            return false
        }

        if(binding.etPasswordSignup.text.toString() == ""){
            binding.etPasswordSignupLayout.error = "This is required field"
            return false
        }

        if(binding.etConfirmPassword.text.toString() == ""){
            binding.etConfirmPasswordLayout.error = "This is required field"
            return false
        }

        if(binding.etPasswordSignup.text.toString() != binding.etConfirmPassword.text.toString()){
            binding.etPasswordSignupLayout.error = "Password do not match"
            return false
        }
        return true
    }

}