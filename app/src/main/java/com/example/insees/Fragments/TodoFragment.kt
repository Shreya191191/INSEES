package com.example.insees.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.insees.R
import com.example.insees.databinding.FragmentTodoBinding

class TodoFragment : Fragment() {
    private lateinit var binding:FragmentTodoBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentTodoBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnTodoAddTask.setOnClickListener {
            val showPopUp=PopUpFragment()
            showPopUp.show((activity as AppCompatActivity).supportFragmentManager,"showPopUp")
        }

    }

}