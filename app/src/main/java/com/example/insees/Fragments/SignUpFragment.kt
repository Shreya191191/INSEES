package com.example.insees.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.insees.R
import com.example.insees.databinding.FragmentSignUpBinding

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
        val email = binding.etEmailSignup.text.toString().trim()
        if (email == ""){
            Toast.makeText(context, "This is required field", Toast.LENGTH_SHORT).show()
            return false
        }
        val t = email.substring(email.length - 14, email.length)

        if(email.length <= 14 || t != "@ei.nits.ac.in"){
            Toast.makeText(context, "Please enter a valid institute id", Toast.LENGTH_SHORT).show()
            return false
        }

        if(binding.etPasswordSignup.text.toString() == "" || binding.etConfirmPassword.text.toString() == ""){
            Toast.makeText(context, "This is required field", Toast.LENGTH_SHORT).show()
            return false
        }

        if(binding.etPasswordSignup.text.toString() != binding.etConfirmPassword.text.toString()){
            Toast.makeText(context, "Both Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

}