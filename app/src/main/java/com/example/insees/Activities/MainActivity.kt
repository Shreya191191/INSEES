package com.example.insees.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.insees.R
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val auth = FirebaseAuth.getInstance()

        if(auth.currentUser != null)
        {
            val verification = auth.currentUser?.isEmailVerified
            if (verification == true)
            {
                val it = Intent(this, HomeActivity::class.java)
                startActivity(it)
                this.finish()
            }
        }
    }
}