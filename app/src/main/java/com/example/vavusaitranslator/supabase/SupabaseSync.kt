package com.example.vavusaitranslator.supabase

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val TAG = "SupabaseSync"
private const val AUDIT_TABLE = "translator_audit_log"

/**
 * Encapsulates the limited set of Supabase operations the app performs. All calls
 * are optional; failures are logged but never surface to the UI so the translator
 * experience remains unaffected if Supabase is misconfigured.
 */
class SupabaseSync(private val clientProvider: () -> SupabaseClient?) {

    suspend fun recordLogin(email: String, baseUrl: String) {
        val client = clientProvider() ?: return
        if (email.isBlank()) return
        val event = AuditEvent(
            email = email,
            baseUrl = baseUrl,
            eventType = "login",
            occurredAt = timestamp(),
            metadata = null
        )
        runCatching {
            client.from(AUDIT_TABLE).insert(event)
        }.onFailure { throwable ->
            Log.w(TAG, "Unable to record login event", throwable)
        }
    }

    suspend fun recordTranslation(
        email: String,
        baseUrl: String,
        sourceLanguage: String,
        targetLanguage: String
    ) {
        val client = clientProvider() ?: return
        if (email.isBlank()) return
        val event = AuditEvent(
            email = email,
            baseUrl = baseUrl,
            eventType = "translation",
            occurredAt = timestamp(),
            metadata = mapOf(
                "source_language" to sourceLanguage,
                "target_language" to targetLanguage
            )
        )
        runCatching {
            client.from(AUDIT_TABLE).insert(event)
        }.onFailure { throwable ->
            Log.w(TAG, "Unable to record translation event", throwable)
        }
    }

    private fun timestamp(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date())
    }
}

@Serializable
private data class AuditEvent(
    val email: String,
    @SerialName("base_url") val baseUrl: String,
    @SerialName("event_type") val eventType: String,
    @SerialName("occurred_at") val occurredAt: String,
    val metadata: Map<String, String>? = null
)