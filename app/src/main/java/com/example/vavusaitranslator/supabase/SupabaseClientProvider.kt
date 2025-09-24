package com.example.vavusaitranslator.supabase

import android.util.Log
import com.example.vavusaitranslator.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Lazily creates a Supabase client when the runtime configuration provides
 * both the project URL and anonymous key. If either value is missing the
 * client will not be created, allowing the rest of the app to function without
 * Supabase enabled.
 */
object SupabaseClientProvider {
    private const val TAG = "SupabaseProvider"

    val client: SupabaseClient? by lazy {
        val url = BuildConfig.SUPABASE_URL
        val anonKey = BuildConfig.SUPABASE_ANON_KEY
        if (url.isBlank() || anonKey.isBlank()) {
            Log.w(TAG, "Supabase URL or anon key missing; Supabase features disabled")
            null
        } else {
            createSupabaseClient(supabaseUrl = url, supabaseKey = anonKey) {
                install(Postgrest)
            }
        }
    }
}