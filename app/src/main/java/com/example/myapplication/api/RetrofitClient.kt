package com.example.myapplication.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client pointing to the deep-search Spring Boot backend.
 *
 * BASE_URL Configuration:
 * - PRODUCTION: https://deep-search-z3bh.onrender.com/ (deployed on Render)
 * - LOCAL (Emulator): http://10.0.2.2:8080/ (maps to host's localhost)
 * - LOCAL (Physical Device): http://[YOUR_IP]:8080/ (your computer's local IP)
 *
 * To switch between environments, change the BASE_URL constant below.
 */
object RetrofitClient {

    // Production URL - deployed deep-search API on Render
    private const val BASE_URL = "https://deep-search-z3bh.onrender.com/"
    
    // Uncomment below for local development:
    // private const val BASE_URL = "http://10.0.2.2:8080/"  // For Android Emulator
    // private const val BASE_URL = "http://192.168.1.100:8080/"  // For Physical Device (replace with your IP)

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val queryApi: QueryApi = retrofit.create(QueryApi::class.java)
}
