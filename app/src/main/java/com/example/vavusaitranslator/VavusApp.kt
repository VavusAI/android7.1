package com.example.vavusaitranslator

import android.app.Application
import com.example.vavusaitranslator.data.LocalLanguageCatalog
import com.example.vavusaitranslator.data.VavusRepository
import com.example.vavusaitranslator.data.SessionManager
import com.example.vavusaitranslator.network.VavusServiceFactory

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

    val repository: VavusRepository = VavusRepository(
        sessionManager = sessionManager,
        serviceFactory = serviceFactory,
        languageCatalog = languageCatalog
    )
    val session: SessionManager = sessionManager
}