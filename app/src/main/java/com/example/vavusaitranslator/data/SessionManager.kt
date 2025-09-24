package com.example.vavusaitranslator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATA_STORE_NAME = "vavus_session"

val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

class SessionManager(context: Context) {
    private val dataStore = context.applicationContext.sessionDataStore

    private object Keys {
        val TOKEN = stringPreferencesKey("auth_token")
        val LEGACY_BASE_URL = stringPreferencesKey("base_url")
        val USERNAME = stringPreferencesKey("username")
        val EMAIL = stringPreferencesKey("email")
    }

    val authToken: Flow<String?> = dataStore.data.map { it[Keys.TOKEN] }
    val email: Flow<String?> = dataStore.data.map { prefs ->
        prefs[Keys.EMAIL] ?: prefs[Keys.USERNAME]
    }
    suspend fun persistSession(token: String, email: String) {
        dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.EMAIL] = email
            // Ensure legacy installs drop the deprecated base URL entry.
            prefs.remove(Keys.LEGACY_BASE_URL)
            prefs.remove(Keys.USERNAME)
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.TOKEN)
            prefs.remove(Keys.EMAIL)
            prefs.remove(Keys.USERNAME)
            prefs.remove(Keys.LEGACY_BASE_URL)
        }
    }
}