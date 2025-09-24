package com.example.madladtranslator.network

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: String? = null,
    val expiresIn: Long? = null
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val orderNumber: String
)

data class RegisterResponse(
    val message: String,
    val token: String? = null
)

data class TranslateRequest(
    val sourceLanguage: String,
    val targetLanguage: String,
    val text: String
)

data class TranslateResponse(
    val translatedText: String,
    val detectedSourceLanguage: String? = null
)