package com.example.insees.network

import android.content.Context
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

object CloudinaryRepository {

    suspend fun uploadImage(
        context: Context,
        imageBytes: ByteArray
    ): String? {

        val file = File(context.cacheDir, "profile.jpg")

        FileOutputStream(file).use {
            it.write(imageBytes)
        }

        val requestFile =
            file.asRequestBody("image/jpeg".toMediaTypeOrNull())

        val body =
            MultipartBody.Part.createFormData(
                "file",
                file.name,
                requestFile
            )

        // 👇 Naya preset name
        val preset =
            "insees_profile_v2"
                .toRequestBody("text/plain".toMediaTypeOrNull())

        val folder =
            "insees/profile"
                .toRequestBody("text/plain".toMediaTypeOrNull())

        val response = RetrofitClient.api.uploadImage(
            body,
            preset,
            folder
        )

        Log.d("UPLOAD", "HTTP Code = ${response.code()}")
        Log.d("UPLOAD", "Success = ${response.isSuccessful}")

        if (response.isSuccessful) {

            Log.d("UPLOAD", "Body = ${response.body()}")

            return response.body()?.secure_url

        } else {

            Log.e("UPLOAD", "Error = ${response.errorBody()?.string()}")

            return null
        }
    }
}