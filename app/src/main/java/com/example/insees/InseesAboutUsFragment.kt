package com.example.insees

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.insees.databinding.FragmentInseesAboutUsBinding


class InseesAboutUsFragment : Fragment() {

    private lateinit var binding: FragmentInseesAboutUsBinding
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
        binding = FragmentInseesAboutUsBinding.inflate(inflater,container,false)

        binding.inseesLayout.setOnClickListener{
            navController.navigate(R.id.action_inseesAboutUsFragment_to_inseesAboutInseesFragment)
        }

        binding.aboutMembers.setOnClickListener {
            navController.navigate(R.id.action_inseesAboutUsFragment_to_aboutMembersFragment)
        }

        binding.imageButton3.setOnClickListener {
            navController.navigate(R.id.action_inseesAboutUsFragment_to_aboutMembersFragment)
        }

        return binding.root
    }

}