package com.example.insees.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.insees.Adapters.ViewPagerAdapter
import com.example.insees.R
import com.google.android.material.bottomnavigation.BottomNavigationView


class ViewPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.fragment_view_pager, container, false)

        val fragmentList = arrayListOf(
            HomeFragment(),
            SemesterFragment(),
            TodoFragment(),
            InseesAboutUsFragment()
        )

        val adapter = ViewPagerAdapter(fragmentList, requireActivity().supportFragmentManager, lifecycle)
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = adapter

        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bvNavBar)

        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    viewPager.currentItem = 0
                    true
                }

                R.id.todoFragment -> {
                    viewPager.currentItem = 2
                    true
                }

                R.id.semesterFragment -> {
                    viewPager.currentItem = 1
                    true
                }

                R.id.inseesAboutUsFragment -> {
                    viewPager.currentItem = 3
                    true
                }
                else -> false
            }
        }
        return view
    }
}