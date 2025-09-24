package com.example.vavusaitranslator.network

import com.example.vavusaitranslator.model.VavusLanguage
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface VavusApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @GET("languages")
    suspend fun languages(): List<VavusLanguage>

    @POST("translate")
    suspend fun translate(
        @Header("Authorization") authorization: String,
        @Body request: TranslateRequest
    ): TranslateResponse
}