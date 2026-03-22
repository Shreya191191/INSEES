package com.example.insees.Utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
object FirebaseManager {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firebaseDatabase: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val firebaseStorage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    @JvmName("functionOfKotlin")
    fun getFirebaseAuth(): FirebaseAuth {
        return firebaseAuth
    }
    @JvmName("functionOfKotlin")
    fun getFirebaseDatabase(): FirebaseDatabase {
        return firebaseDatabase
    }
    @JvmName("functionOfKotlin")
    fun getFirebaseStorage(): FirebaseStorage {
        return firebaseStorage
    }
}