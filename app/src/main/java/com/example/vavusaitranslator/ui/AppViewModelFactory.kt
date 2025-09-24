package com.example.vavusaitranslator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vavusaitranslator.data.VavusRepository
import com.example.vavusaitranslator.data.SessionManager

class AppViewModelFactory(
    private val repository: VavusRepository,
    private val session: SessionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository, session)
        modelClass.isAssignableFrom(TranslatorViewModel::class.java) -> TranslatorViewModel(repository, session)
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    } as T
}