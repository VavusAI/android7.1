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
        val BASE_URL = stringPreferencesKey("base_url")
        val USERNAME = stringPreferencesKey("username")
    }

    val authToken: Flow<String?> = dataStore.data.map { it[Keys.TOKEN] }
    val baseUrl: Flow<String?> = dataStore.data.map { it[Keys.BASE_URL] }
    val username: Flow<String?> = dataStore.data.map { it[Keys.USERNAME] }

    suspend fun persistSession(token: String, baseUrl: String, username: String) {
        dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.BASE_URL] = baseUrl
            prefs[Keys.USERNAME] = username
        }
    }

    suspend fun updateBaseUrl(baseUrl: String) {
        dataStore.edit { prefs ->
            prefs[Keys.BASE_URL] = baseUrl
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.TOKEN)
            prefs.remove(Keys.BASE_URL)
            prefs.remove(Keys.USERNAME)
        }
    }
}