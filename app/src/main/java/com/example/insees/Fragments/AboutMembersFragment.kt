package com.example.insees.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.insees.databinding.FragmentAboutMembersBinding



class AboutMembersFragment : Fragment() {
    lateinit var binding: FragmentAboutMembersBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentAboutMembersBinding.inflate(inflater, container, false)
        val view = binding.root

        return view

    }
}