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
 * Data class for sending OTP request.
 */
data class SendOtpRequest(
    val phoneNumber: String
)

/**
 * Data class for verifying OTP request.
 */
data class VerifyOtpRequest(
    val phoneNumber: String,
    val otpCode: String
)

/**
 * Data class for OTP response.
 */
data class OtpResponse(
    val success: Boolean = false,
    val message: String? = null,
    val phoneNumber: String? = null
)

/**
 * Data class for the registration request body.
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String? = null,
    val profileImageUrl: String? = null
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
    val role: String? = null,
    val profileImageUrl: String? = null
)

/**
 * Retrofit API interface for authentication endpoints.
 * Connects to the deep-search Spring Boot backend.
 *
 * Endpoints:
 *   POST /api/auth/login    — login with email & password
 *   POST /api/auth/register — register with name, email & password
 *   POST /api/auth/send-otp — send OTP to phone number
 *   POST /api/auth/verify-otp — verify OTP and login
 */
interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<OtpResponse>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>
}
