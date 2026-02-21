package com.example.myapplication.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query

data class ServiceItem(
    val serviceId: Int,
    val serviceName: String,
    val description: String? = null,
    val price: Double? = null,
    val duration: String? = null,
    val serviceTypes: List<ServiceTypeItem> = emptyList(),
    val mainImageUrl: String? = null,
    val galleryPhotoUrls: List<String> = emptyList()
)

data class ServiceTypeItem(
    val serviceTypeId: Int? = null,
    val typeName: String,
    val typeDescription: String? = null,
    val subscriptionPlan: String? = null,
    val typePrice: Double,
    val photoUrls: List<String> = emptyList()
)

data class CreateServiceRequest(
    val serviceName: String,
    val description: String? = null,
    val price: Double? = null,
    val duration: String? = null,
    val serviceTypes: List<ServiceTypeItem> = emptyList(),
    val mainImageUrl: String? = null,
    val galleryPhotoUrls: List<String> = emptyList()
)

data class LabourRequestSubmitRequest(
    val token: String,
    val serviceId: Int,
    val address: String,
    val preferredDate: String? = null,
    val notes: String? = null
)

data class LabourRequestResponse(
    val success: Boolean = false,
    val message: String? = null,
    val requestId: Long? = null,
    val serviceId: Int? = null,
    val serviceName: String? = null,
    val userEmail: String? = null,
    val address: String? = null,
    val preferredDate: String? = null,
    val status: String? = null,
    val createdAt: String? = null
)

data class UserLabourRequestItem(
    val requestId: Long? = null,
    val serviceId: Int? = null,
    val serviceName: String? = null,
    val address: String? = null,
    val preferredDate: String? = null,
    val notes: String? = null,
    val status: String? = null,
    val createdAt: String? = null
)

data class UserLabourRequestsResponse(
    val success: Boolean = false,
    val message: String? = null,
    val requests: List<UserLabourRequestItem> = emptyList()
)

data class LabourServiceMapRequest(
    val token: String,
    val serviceId: Int,
    val experienceYears: Int? = null,
    val notes: String? = null
)

data class LabourServiceMappingResponse(
    val success: Boolean = false,
    val message: String? = null,
    val mappingId: Long? = null,
    val serviceId: Int? = null,
    val serviceName: String? = null,
    val experienceYears: Int? = null,
    val notes: String? = null,
    val createdAt: String? = null
)

data class MappedServiceItem(
    val mappingId: Long? = null,
    val labourUserId: Int? = null,
    val labourName: String? = null,
    val labourEmail: String? = null,
    val serviceId: Int? = null,
    val serviceName: String? = null,
    val description: String? = null,
    val experienceYears: Int? = null,
    val notes: String? = null,
    val createdAt: String? = null
)

data class LabourMappedServicesResponse(
    val success: Boolean = false,
    val message: String? = null,
    val services: List<MappedServiceItem> = emptyList()
)

interface LabourApi {
    @POST("api/services")
    suspend fun createService(@Body request: CreateServiceRequest): Response<ServiceItem>

    @GET("api/services")
    suspend fun getServices(): Response<List<ServiceItem>>

    @POST("api/labour-requests/submit")
    suspend fun submitLabourRequest(@Body request: LabourRequestSubmitRequest): Response<LabourRequestResponse>

    @GET("api/labour-requests/my")
    suspend fun getMyLabourRequests(@Query("token") token: String): Response<UserLabourRequestsResponse>

    @POST("api/labour-services/map")
    suspend fun mapService(@Body request: LabourServiceMapRequest): Response<LabourServiceMappingResponse>

    @GET("api/labour-services/my")
    suspend fun getMappedServices(
        @Query("token") token: String,
        @Query("search") search: String? = null
    ): Response<LabourMappedServicesResponse>

    @GET("api/labour-services/available")
    suspend fun getAvailableMappedServices(
        @Query("token") token: String,
        @Query("search") search: String? = null
    ): Response<LabourMappedServicesResponse>
}


