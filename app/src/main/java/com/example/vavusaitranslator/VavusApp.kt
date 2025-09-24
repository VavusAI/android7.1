package com.example.vavusaitranslator

import android.app.Application
import com.example.vavusaitranslator.BuildConfig
import com.example.vavusaitranslator.data.LocalLanguageCatalog
import com.example.vavusaitranslator.data.VavusRepository
import com.example.vavusaitranslator.data.SessionManager
import com.example.vavusaitranslator.network.VavusServiceFactory
import com.example.vavusaitranslator.supabase.SupabaseClientProvider
import com.example.vavusaitranslator.supabase.SupabaseSync

class VavusApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(application: Application) {
    private val sessionManager = SessionManager(application)
    private val serviceFactory = VavusServiceFactory()
    private val languageCatalog = LocalLanguageCatalog(application)
    private val supabaseSync = SupabaseSync { SupabaseClientProvider.client }

    val repository: VavusRepository = VavusRepository(
        sessionManager = sessionManager,
        serviceFactory = serviceFactory,
        languageCatalog = languageCatalog,
        translatorBaseUrl = BuildConfig.TRANSLATOR_API_BASE_URL,
        supabaseSync = supabaseSync
    )
    val session: SessionManager = sessionManager
}