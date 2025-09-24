package com.example.madladtranslator

import android.app.Application
import com.example.madladtranslator.data.LocalLanguageCatalog
import com.example.madladtranslator.data.MadladRepository
import com.example.madladtranslator.data.SessionManager
import com.example.madladtranslator.network.MadladServiceFactory

class MadladApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(application: Application) {
    private val sessionManager = SessionManager(application)
    private val serviceFactory = MadladServiceFactory()
    private val languageCatalog = LocalLanguageCatalog(application)

    val repository: MadladRepository = MadladRepository(
        sessionManager = sessionManager,
        serviceFactory = serviceFactory,
        languageCatalog = languageCatalog
    )
    val session: SessionManager = sessionManager
}