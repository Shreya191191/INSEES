package com.example.insees.Activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.insees.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var bottomNavigationView : BottomNavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navImageView: CircleImageView
    private lateinit var navName: TextView
    private lateinit var navEmail: TextView
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

//        window.setDecorFitsSystemWindows(false)
//        window.insetsController?.hide(WindowInsets.Type.statusBars())
//
//        window.decorView.systemUiVisibility = (
//                View.SYSTEM_UI_FLAG_FULLSCREEN or
//                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
//                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                )
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)

        navImageView = headerView.findViewById(R.id.navImageView)
        navName = headerView.findViewById(R.id.navName)
        navEmail = headerView.findViewById(R.id.navEmail)

        drawerLayout = findViewById(R.id.drawer_layout)
        bottomNavigationView = findViewById(R.id.bvNavBar)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_home) as NavHostFragment
        navController = navHostFragment.navController

        navigationView.setNavigationItemSelectedListener(this)

    }

    fun toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    fun loadCircleImage(localFile: File){
        Glide.with(this)
            .load(localFile)
            .placeholder(R.drawable.ic_userr_foreground)
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Enable disk caching
            .into(navImageView)
    }

    fun updateNameAndEmail(name: String, email: String){
        navName.text = name
        navEmail.text = email
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_logout -> logout()
        }
        drawerLayout.closeDrawer(GravityCompat.END)
        return true
    }
    private fun logout() {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show()
        val it = Intent(this, MainActivity::class.java)
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
        this.finish()
    }
}