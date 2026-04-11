package com.example.insees.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.insees.R
import com.example.insees.activity.HomeActivity
import com.example.insees.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentLoginBinding
    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = view.findNavController()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater,container,false)

        auth = FirebaseAuth.getInstance()

        binding.tvCreateNewAccount.setOnClickListener{
            navController.navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmailLogin.text.toString().trim()
            val password = binding.etPasswordLogin.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                login(email, password)
            }
            else
            {
                Toast.makeText(requireContext(), "Enter Your Details", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnForgetPassword.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
        return binding.root
    }
    private fun login(email:String, password:String){
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{task->
            if (task.isSuccessful){
                val verification = auth.currentUser?.isEmailVerified
                when(verification){
                    true -> {
                        val intent = Intent(context, HomeActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    else -> {
                        auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "Please Verify Your Email",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                            ?.addOnFailureListener{
                                Toast.makeText(requireContext(), "Failed to send verification email: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                    }
                }
            }
        }.addOnFailureListener {exception->
            Toast.makeText(context, exception.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }
}