package com.example.insees

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.insees.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.btnNextSignUp.setOnClickListener {
            if (checkAllFields()) {
                navController.navigate(R.id.action_signUpFragment_to_completeProfileFragment)
            }
        }

        binding.tvLogin.setOnClickListener {
            navController.navigate(R.id.action_signUpFragment_to_loginFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun checkAllFields(): Boolean {
        val email = binding.etEmailSignup.text.toString()
        if (email == "") {
            binding.etEmailSignup.error = "This is required field"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmailSignup.error = "Please enter a valid Email"
            return false
        }

        if (binding.etPassword.text.toString() == "") {
            binding.etPassword.error = "This is required field"
            return false
        }

        if (binding.etConfirmPassword.text.toString() == "") {
            binding.etConfirmPassword.error = "This is required field"
            return false
        }

        if (binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString()) {
            binding.etPassword.error = "Password do not match"
            return false
        }

        return true
    }
}