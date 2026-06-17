package com.example.insees.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logger)
        .build()

    val api: CloudinaryApi by lazy {

        Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/v1_1/dhmftd2zq/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudinaryApi::class.java)

    }

}