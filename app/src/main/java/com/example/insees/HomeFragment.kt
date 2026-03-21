package com.example.insees

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.insees.databinding.FragmentHomeBinding
import com.example.insees.databinding.FragmentIntroBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
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
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.btnViewAll.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_todoFragment)
        }

        binding.btnAddTask.setOnClickListener{
            navController.navigate(R.id.action_homeFragment_to_popUpFragment)
        }

        return binding.root
    }
}