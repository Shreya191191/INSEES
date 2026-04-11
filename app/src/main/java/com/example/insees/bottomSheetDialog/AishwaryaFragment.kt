package com.example.insees.bottomSheetDialog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.insees.databinding.FragmentAishwaryaBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class AishwaryaFragment : BottomSheetDialogFragment() {
    private lateinit var binding:FragmentAishwaryaBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentAishwaryaBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gitIcon.setOnClickListener {
            val intent= Intent(Intent.ACTION_VIEW)
            intent.data= Uri.parse("https://github.com/Aishwarya191sh")
            startActivity(intent)
        }

        binding.linkedInIcon.setOnClickListener {
            val intent= Intent(Intent.ACTION_VIEW)
            intent.data= Uri.parse("https://www.linkedin.com/in/aishwarya-shukla-1b733a2a9/")
            startActivity(intent)
        }

        binding.instaIcon.setOnClickListener {
            val intent= Intent(Intent.ACTION_VIEW)
            intent.data= Uri.parse("https://www.instagram.com/_aishwaryashukla__/?hl=en")
            startActivity(intent)
        }

    }

}