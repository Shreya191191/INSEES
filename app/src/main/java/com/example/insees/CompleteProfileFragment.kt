package com.example.insees

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.insees.databinding.FragmentCompleteProfileBinding

class CompleteProfileFragment : Fragment() {

    private lateinit var binding: FragmentCompleteProfileBinding
    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCompleteProfileBinding.inflate(inflater,container, false)

        binding.openCamera.setOnClickListener{

        }

        binding.uploadImage.setOnClickListener{

        }

        binding.btnNextCompleteProfile.setOnClickListener {

        }

        return binding.root
    }

}