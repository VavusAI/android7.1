package com.example.vavusaitranslator.data

import com.example.vavusaitranslator.model.VavusLanguage
import com.example.vavusaitranslator.network.LoginRequest
import com.example.vavusaitranslator.network.VavusApi
import com.example.vavusaitranslator.network.VavusServiceFactory
import com.example.vavusaitranslator.network.RegisterRequest
import com.example.vavusaitranslator.network.TranslateRequest
import com.example.vavusaitranslator.supabase.SupabaseSync
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class VavusRepository(
    private val sessionManager: SessionManager,
    private val serviceFactory: VavusServiceFactory,
    private val languageCatalog: LocalLanguageCatalog,
    translatorBaseUrl: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val supabaseSync: SupabaseSync? = null
) {
    private val configuredBaseUrl = translatorBaseUrl.trim()

    private val api: VavusApi by lazy {
        val baseUrl = configuredBaseUrl.takeIf { it.isNotBlank() }
            ?: error("Translator API base URL missing")
        serviceFactory.create(baseUrl)
    }

    private fun requireApi(): VavusApi = api

    suspend fun login(username: String, password: String): Result<Unit> = runCatching {
        withContext(ioDispatcher) {
            val api = requireApi()
            val response = api.login(LoginRequest(username = username, password = password))
            sessionManager.persistSession(
                token = response.token,
                username = username
            )
            supabaseSync?.recordLogin(username = username, baseUrl = configuredBaseUrl)
        }
    }

    suspend fun register(username: String, password: String, orderNumber: String): Result<Unit> = runCatching {
        withContext(ioDispatcher) {
            val api = requireApi()
            val response = api.register(
                RegisterRequest(
                    username = username,
                    password = password,
                    orderNumber = orderNumber
                )
            )
            response.token?.let { token ->
                sessionManager.persistSession(token = token, username = username)
                supabaseSync?.recordLogin(username = username, baseUrl = configuredBaseUrl)
            }
        }
    }

    suspend fun fetchLanguages(): Result<List<VavusLanguage>> = runCatching {
        withContext(ioDispatcher) {
            val api = requireApi()
            api.languages().sortedBy { it.name }
        }
    }

    fun fallbackLanguages(): List<VavusLanguage> = languageCatalog.getAll()

    suspend fun translate(
        sourceLanguage: String,
        targetLanguage: String,
        text: String
    ): Result<String> = runCatching {
        withContext(ioDispatcher) {
            val token = sessionManager.authToken.first()
            require(!token.isNullOrBlank()) { "Authentication token missing" }
            val api = requireApi()
            val response = api.translate(
                authorization = "Bearer $token",
                request = TranslateRequest(
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    text = text
                )
            )
            val translation = response.translatedText
            val username = sessionManager.username.first().orEmpty()
            supabaseSync?.recordTranslation(
                username = username,
                baseUrl = configuredBaseUrl,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage
            )
            translation
        }
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }
}