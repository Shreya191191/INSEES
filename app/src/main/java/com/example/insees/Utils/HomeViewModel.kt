package com.example.insees.Utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _userData = MutableLiveData<String>()
    val userData: LiveData<String> get() = _userData

    fun fetchUserData() {
        // Fetch user data from Firebase and update _userData LiveData
        // Example:
        _loading.value = true
        val currentUser = FirebaseManager.getFirebaseAuth().currentUser
        currentUser?.let { user ->
            val databaseRef = FirebaseManager.getFirebaseDatabase().reference
            val userRef = databaseRef.child("users").child(user.uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val name = dataSnapshot.child("name").getValue(String::class.java)
                    name?.let {
                        _userData.value = "Hello $it"
                        _loading.value = false
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }
}