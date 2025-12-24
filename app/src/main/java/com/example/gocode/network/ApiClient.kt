package com.example.gocode.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // אמולטור: 10.0.2.2
    // מכשיר פיזי: תחליף ל-IP של המחשב שלך
    private const val BASE_URL = "http://72.20.17.70:8000/"

    val execApi: ExecApi by lazy {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExecApi::class.java)
    }
}
