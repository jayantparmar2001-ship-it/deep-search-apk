package com.example.myapplication.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Data class for the customer query request body.
 */
data class CustomerQueryRequest(
    val email: String,
    val subject: String,
    val message: String
)

/**
 * Data class for the customer query response from deep-search backend.
 */
data class CustomerQueryResponse(
    val success: Boolean = false,
    val message: String? = null,
    val queryId: Long? = null,
    val email: String? = null,
    val subject: String? = null,
    val createdAt: String? = null,
    val status: String? = null
)

data class UserQueryItem(
    val queryId: Long? = null,
    val subject: String? = null,
    val message: String? = null,
    val createdAt: String? = null,
    val status: String? = null
)

data class UserQueriesResponse(
    val success: Boolean = false,
    val message: String? = null,
    val queries: List<UserQueryItem> = emptyList()
)

/**
 * Retrofit API interface for customer query endpoints.
 * Connects to the deep-search Spring Boot backend.
 *
 * Endpoints:
 *   POST /api/queries/submit — submit a customer query/inquiry
 */
interface QueryApi {

    @POST("api/queries/submit")
    suspend fun submitQuery(@Body request: CustomerQueryRequest): Response<CustomerQueryResponse>

    @GET("api/queries/user/{email}")
    suspend fun getUserQueries(@Path("email", encoded = true) email: String): Response<UserQueriesResponse>
}


