package com.example.insees.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.insees.R
import com.example.insees.databinding.FragmentSemesterBinding

class SemesterFragment : Fragment() {

    private lateinit var parentRecyclerView: RecyclerView
    private lateinit var binding:FragmentSemesterBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentSemesterBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentRecyclerView=view.findViewById(R.id.parentRecyclerView)
        parentRecyclerView.setHasFixedSize(true)
        parentRecyclerView.layoutManager=LinearLayoutManager(context)



        binding.btnSemesterBack.setOnClickListener {
            findNavController().navigate(R.id.action_semesterFragment_to_todoFragment)
        }

    }

}