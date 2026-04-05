package com.example.insees.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.insees.Adapters.ViewPagerAdapter
import com.example.insees.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class ViewPagerFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_pager, container, false)

        val fragmentList = arrayListOf(
            HomeFragment(),
            SemesterFragment(),
            TodoFragment(),
            InseesAboutUsFragment()
        )

        val adapter = ViewPagerAdapter(fragmentList, childFragmentManager, lifecycle)
        viewPager = view.findViewById(R.id.viewPager)
        viewPager.adapter = adapter

        bottomNavigationView = requireActivity().findViewById(R.id.bvNavBar)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })

        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    setViewPagerItem(0)
                    true
                }
                R.id.todoFragment -> {
                    setViewPagerItem(2)
                    true
                }
                R.id.semesterFragment -> {
                    setViewPagerItem(1)
                    true
                }
                R.id.inseesAboutUsFragment -> {
                    setViewPagerItem(3)
                    true
                }
                else -> false
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewPager.currentItem != 0) {
                    setViewPagerItem(0)
                } else {
                    requireActivity().finish()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun setViewPagerItem(position: Int) {
        viewPager.setCurrentItem(position, false)
    }
}