package com.example.insees

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.insees.databinding.FragmentIntroBinding

class IntroFragment : Fragment() {

    private lateinit var binding: FragmentIntroBinding
    private lateinit var navController:NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentIntroBinding.inflate(inflater,container,false)

        binding.alreadyHaveAccount.setOnClickListener{
            navController.navigate(R.id.action_introFragment_to_loginFragment)
        }

        binding.newUser.setOnClickListener {
            navController.navigate(R.id.action_introFragment_to_signUpFragment)
        }
        return binding.root
    }
}