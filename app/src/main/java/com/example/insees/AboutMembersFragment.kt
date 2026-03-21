package com.example.insees

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.insees.databinding.FragmentAboutMembersBinding

class AboutMembersFragment : Fragment() {

    private lateinit var binding: FragmentAboutMembersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding=FragmentAboutMembersBinding.inflate(inflater,container,false)

        binding.btnSemesterBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return binding.root

    }


}