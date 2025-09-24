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

private const val OFFLINE_TOKEN = "offline-preview-token"

class VavusRepository(
    private val sessionManager: SessionManager,
    private val serviceFactory: VavusServiceFactory,
    private val languageCatalog: LocalLanguageCatalog,
    translatorBaseUrl: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val supabaseSync: SupabaseSync? = null
) {
    private val configuredBaseUrl = translatorBaseUrl.trim()

    private val api: VavusApi? by lazy {
        configuredBaseUrl.takeIf { it.isNotBlank() }?.let { baseUrl ->
            serviceFactory.create(baseUrl)
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        withContext(ioDispatcher) {
            val api = this@VavusRepository.api
            if (api == null) {
                sessionManager.persistSession(token = OFFLINE_TOKEN, email = email)
                return@withContext
            }
            val response = api.login(LoginRequest(email = email, password = password))
            sessionManager.persistSession(
                token = response.token,
                email = email
            )
            if (configuredBaseUrl.isNotBlank()) {
                supabaseSync?.recordLogin(email = email, baseUrl = configuredBaseUrl)
            }
        }
    }

    suspend fun register(email: String, password: String, orderNumber: String): Result<Unit> = runCatching {
        withContext(ioDispatcher) {
            val api = this@VavusRepository.api
            if (api == null) {
                sessionManager.persistSession(token = OFFLINE_TOKEN, email = email)
                return@withContext
            }
            val response = api.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    orderNumber = orderNumber
                )
            )
            response.token?.let { token ->
                sessionManager.persistSession(token = token, email = email)
                if (configuredBaseUrl.isNotBlank()) {
                    supabaseSync?.recordLogin(email = email, baseUrl = configuredBaseUrl)
                }
            }
        }
    }

    suspend fun fetchLanguages(): Result<List<VavusLanguage>> = runCatching {
        withContext(ioDispatcher) {
            val api = this@VavusRepository.api
            if (api == null) {
                languageCatalog.getAll().sortedBy { it.name }
            } else {
                api.languages().sortedBy { it.name }
            }
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
            val api = this@VavusRepository.api
                ?: throw IllegalStateException("Translator service is not configured. Please set the API base URL before translating.")
            val response = api.translate(
                authorization = "Bearer $token",
                request = TranslateRequest(
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    text = text
                )
            )
            val translation = response.translatedText
            val email = sessionManager.email.first().orEmpty()
            if (configuredBaseUrl.isNotBlank()) {
                supabaseSync?.recordTranslation(
                    email = email,
                    baseUrl = configuredBaseUrl,
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage
                )
            }
            translation
        }
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }
}