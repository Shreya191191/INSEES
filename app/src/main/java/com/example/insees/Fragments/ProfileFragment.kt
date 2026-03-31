package com.example.insees.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.insees.Activities.MainActivity
import com.example.insees.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var navController : NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.btnBackToHome.setOnClickListener {
            navController.popBackStack()
        }
        binding.btnLogOut.setOnClickListener {
            logout()
        }
        return (view)
    }

    private fun logout() {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        Toast.makeText(requireContext(), "Logged Out Successfully", Toast.LENGTH_SHORT).show()
        val it = Intent(requireContext(), MainActivity::class.java)
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
        requireActivity().finish()
    }
}