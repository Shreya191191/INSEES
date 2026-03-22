package com.example.insees.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.insees.Dataclasses.GridViewModal
import com.example.insees.Utils.GridRVAdapter
import com.example.insees.R
import com.example.insees.databinding.FragmentInseesAboutInseesBinding
import kotlin.collections.plus

/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [InseesAboutInseesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InseesAboutInseesFragment : Fragment() {
    lateinit var MemberGRV: GridView
    lateinit var MemberList: List<GridViewModal>
    lateinit var binding: FragmentInseesAboutInseesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentInseesAboutInseesBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.btnBackHomeFromInsees.setOnClickListener {
            findNavController().navigate(R.id.action_inseesAboutInseesFragment_to_homeFragment)
        }
        MemberGRV = view.findViewById(R.id.idgv)
        MemberList = ArrayList<GridViewModal>()

        // Adding data to our course list with image and course name.
        MemberList = MemberList + GridViewModal(
            "Name",
            "loprem ipsum Description qwertyuiopasdfghjklzxcvbnm",
            R.drawable.sample_img
        )
        MemberList = MemberList + GridViewModal(
            "Name",
            "loprem ipsum Description qwertyuiopasdfghjklzxcvbnm",
            R.drawable.sample_img
        )
        MemberList = MemberList + GridViewModal(
            "Name",
            "loprem ipsum Description qwertyuiopasdfghjklzxcvbnm",
            R.drawable.sample_img
        )
        MemberList = MemberList + GridViewModal(
            "Name",
            "loprem ipsum Description qwertyuiopasdfghjklzxcvbnm",
            R.drawable.sample_img
        )
        MemberList = MemberList + GridViewModal(
            "Name",
            "loprem ipsum Description qwertyuiopasdfghjklzxcvbnm",
            R.drawable.sample_img
        )

        // Initializing our course adapter and passing course list and context.
        val courseAdapter = GridRVAdapter(MemberList = MemberList, requireContext())

        // Setting adapter to our grid view.
        MemberGRV.adapter = courseAdapter

        return view

    }
}