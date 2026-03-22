package com.example.insees.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.insees.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI

class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bottomNavigationView = findViewById(R.id.bvNavBar)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_home) as NavHostFragment
        navController = navHostFragment.navController

        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        // Custom navigation handling
        bottomNavigationView.setOnItemSelectedListener { item ->
            val currentDestination = navController.currentDestination?.id

            when (item.itemId) {
                R.id.homeFragment -> {
                    if (currentDestination != R.id.homeFragment) {
                        navController.popBackStack()
                        navController.navigate(R.id.homeFragment)
                    }
                    true
                }

                R.id.todoFragment -> {
                    if (currentDestination != R.id.todoFragment) {
                        navController.popBackStack()
                        navController.navigate(R.id.todoFragment)
                    }
                    true
                }

                R.id.semesterFragment -> {
                    if (currentDestination != R.id.semesterFragment) {
                        navController.popBackStack()
                        navController.navigate(R.id.semesterFragment)
                    }
                    true
                }

                R.id.inseesAboutUsFragment -> {
                    if (currentDestination != R.id.inseesAboutUsFragment) {
                        navController.popBackStack()
                        navController.navigate(R.id.inseesAboutUsFragment)
                    }
                    true
                }

                else -> false
            }
        }
    }
}