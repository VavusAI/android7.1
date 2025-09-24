package com.example.madladtranslator.data

import com.example.madladtranslator.model.MadladLanguage
import com.example.madladtranslator.network.LoginRequest
import com.example.madladtranslator.network.MadladApi
import com.example.madladtranslator.network.MadladServiceFactory
import com.example.madladtranslator.network.RegisterRequest
import com.example.madladtranslator.network.TranslateRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class MadladRepository(
    private val sessionManager: SessionManager,
    private val serviceFactory: MadladServiceFactory,
    private val languageCatalog: LocalLanguageCatalog,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val apiState: MutableStateFlow<MadladApi?> = MutableStateFlow(null)

    val activeBaseUrl: Flow<String?> = sessionManager.baseUrl

    suspend fun updateBaseUrl(baseUrl: String) {
        sessionManager.updateBaseUrl(baseUrl)
        apiState.update { serviceFactory.create(baseUrl) }
    }

    private suspend fun ensureApi(baseUrl: String? = null): MadladApi {
        val cached = apiState.value
        val resolvedBaseUrl = baseUrl ?: sessionManager.baseUrl.first()
        if (cached != null && baseUrl == null) {
            return cached
        }
        require(!resolvedBaseUrl.isNullOrBlank()) { "Base URL is required" }
        val api = serviceFactory.create(resolvedBaseUrl)
        apiState.value = api
        return api
    }

    suspend fun login(baseUrl: String, username: String, password: String): Result<Unit> = runCatching {
        withContext(ioDispatcher) {
            val api = ensureApi(baseUrl)
            val response = api.login(LoginRequest(username = username, password = password))
            sessionManager.persistSession(
                token = response.token,
                baseUrl = baseUrl,
                username = username
            )
            apiState.value = serviceFactory.create(baseUrl)
        }
    }

    suspend fun register(baseUrl: String, username: String, password: String, orderNumber: String): Result<Unit> = runCatching {
        withContext(ioDispatcher) {
            val api = ensureApi(baseUrl)
            val response = api.register(
                RegisterRequest(
                    username = username,
                    password = password,
                    orderNumber = orderNumber
                )
            )
            response.token?.let { token ->
                sessionManager.persistSession(token = token, baseUrl = baseUrl, username = username)
                apiState.value = serviceFactory.create(baseUrl)
            }
        }
    }

    suspend fun fetchLanguages(): Result<List<MadladLanguage>> = runCatching {
        withContext(ioDispatcher) {
            val api = ensureApi()
            api.languages().sortedBy { it.name }
        }
    }

    fun fallbackLanguages(): List<MadladLanguage> = languageCatalog.getAll()

    suspend fun translate(
        sourceLanguage: String,
        targetLanguage: String,
        text: String
    ): Result<String> = runCatching {
        withContext(ioDispatcher) {
            val token = sessionManager.authToken.first()
            require(!token.isNullOrBlank()) { "Authentication token missing" }
            val api = ensureApi()
            val response = api.translate(
                authorization = "Bearer $token",
                request = TranslateRequest(
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    text = text
                )
            )
            response.translatedText
        }
    }

    suspend fun logout() {
        sessionManager.clearSession()
        apiState.value = null
    }
}