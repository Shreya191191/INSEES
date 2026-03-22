package com.example.insees.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.insees.Dataclasses.GridViewModal
import com.example.insees.Adapters.GridRVAdapter
import com.example.insees.R
import com.example.insees.databinding.FragmentInseesAboutInseesBinding



class InseesAboutInseesFragment : Fragment() {
    lateinit var binding: FragmentInseesAboutInseesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentInseesAboutInseesBinding.inflate(inflater, container, false)
        val view = binding.root

        return view

    }
}