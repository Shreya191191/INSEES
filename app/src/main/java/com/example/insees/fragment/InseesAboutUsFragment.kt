package com.example.insees.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.insees.R
import com.example.insees.databinding.FragmentInseesAboutUsBinding


class InseesAboutUsFragment : Fragment() {

    private lateinit var binding: FragmentInseesAboutUsBinding
    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentInseesAboutUsBinding.inflate(inflater,container,false)

        val membersFragment = AboutMembersFragment()
        val aboutinseesFragment = AboutDevelopersFragment()

        binding.membersbtn.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), R.color.darkGreen)
        )

        childFragmentManager.beginTransaction().apply {
            replace(R.id.flfragmentaboutus,membersFragment)
            commit()
        }

        binding.membersbtn.setOnClickListener {
            // Change button colors when "Members" button is selected
            binding.membersbtn.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.darkGreen)
            )
            binding.developersbtn.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.customgreen)
            )

            childFragmentManager.beginTransaction().apply {
                replace(R.id.flfragmentaboutus, membersFragment)
                commit()
            }
        }

        binding.developersbtn.setOnClickListener {
            // Change button colors when "Developers" button is selected
            binding.developersbtn.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.darkGreen)
            )
            binding.membersbtn.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.customgreen)
            )

            childFragmentManager.beginTransaction().apply {
                replace(R.id.flfragmentaboutus, aboutinseesFragment)
                commit()
            }
        }

        return binding.root
    }

}