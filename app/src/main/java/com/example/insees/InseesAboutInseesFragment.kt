package com.example.insees

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import android.widget.Toast
import com.example.insees.Dataclasses.GridViewModal
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [InseesAboutInseesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InseesAboutInseesFragment : Fragment() {
    lateinit var MemberGRV: GridView
    lateinit var MemberList: List<GridViewModal>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =inflater.inflate(R.layout.fragment_insees_about_insees, container, false)

        MemberGRV = view.findViewById(R.id.idgv)
        MemberList = ArrayList<GridViewModal>()

        // Adding data to our course list with image and course name.
        MemberList = MemberList + GridViewModal("Name","loprem ipsum Description qwertyuiopasdfghjklzxcvbnm" ,R.drawable.sample_img)
        MemberList = MemberList + GridViewModal("Name","loprem ipsum Description qwertyuiopasdfghjklzxcvbnm" ,R.drawable.sample_img)
        MemberList = MemberList + GridViewModal("Name","loprem ipsum Description qwertyuiopasdfghjklzxcvbnm" ,R.drawable.sample_img)
        MemberList = MemberList + GridViewModal("Name","loprem ipsum Description qwertyuiopasdfghjklzxcvbnm" ,R.drawable.sample_img)
        MemberList = MemberList + GridViewModal("Name","loprem ipsum Description qwertyuiopasdfghjklzxcvbnm" ,R.drawable.sample_img)

        // Initializing our course adapter and passing course list and context.
        val courseAdapter = GridRVAdapter(MemberList = MemberList, requireContext())

        // Setting adapter to our grid view.
        MemberGRV.adapter = courseAdapter

        return view

    }
}