package com.example.insees

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView

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

        NavigationUI.setupWithNavController(
            bottomNavigationView, navController
        )

        bottomNavigationView.setOnItemSelectedListener{item ->
            when(item.itemId) {
                R.id.homeFragment -> {

                    navController.navigate(R.id.homeFragment)
                    true
                }

                R.id.todoFragment -> {
                    navController.navigate(R.id.todoFragment)
                    true
                }

                R.id.semesterFragment -> {
                    navController.navigate(R.id.semesterFragment)
                    true
                }

                R.id.inseesAboutUsFragment -> {
                    navController.navigate(R.id.inseesAboutUsFragment)
                    true
                }

                else -> false
            }
        }
    }
}