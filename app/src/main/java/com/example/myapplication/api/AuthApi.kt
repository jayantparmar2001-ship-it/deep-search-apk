package com.example.myapplication.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Data class for the login request body.
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Data class for the registration request body.
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String? = null
)

/**
 * Data class for the auth response from deep-search backend.
 */
data class AuthResponse(
    val success: Boolean = false,
    val message: String? = null,
    val token: String? = null,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null
)

/**
 * Retrofit API interface for authentication endpoints.
 * Connects to the deep-search Spring Boot backend.
 *
 * Endpoints:
 *   POST /api/auth/login    — login with email & password
 *   POST /api/auth/register — register with name, email & password
 */
interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}
