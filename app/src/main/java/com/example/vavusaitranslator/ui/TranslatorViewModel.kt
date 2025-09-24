package com.example.vavusaitranslator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vavusaitranslator.data.VavusRepository
import com.example.vavusaitranslator.data.SessionManager
import com.example.vavusaitranslator.model.VavusLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TranslatorViewModel(
    private val repository: VavusRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TranslatorUiState())
    val uiState: StateFlow<TranslatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            session.username.collectLatest { username ->
                _uiState.update { it.copy(username = username.orEmpty()) }
            }
        }
    }

    fun refreshLanguages() {
        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.fetchLanguages()
            _uiState.update { state ->
                result.fold(
                    onSuccess = { languages ->
                        val source = state.sourceLanguage ?: languages.firstOrNull()
                        val target = state.targetLanguage ?: languages.getOrNull(1)
                        state.copy(
                            isLoading = false,
                            languages = languages,
                            sourceLanguage = source,
                            targetLanguage = target,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        val fallback = repository.fallbackLanguages()
                        if (fallback.isNotEmpty()) {
                            val source = fallback.firstOrNull()
                            val target = fallback.getOrNull(1)
                            state.copy(
                                isLoading = false,
                                languages = fallback,
                                sourceLanguage = source,
                                targetLanguage = target,
                                error = "Using offline Vavus catalog: ${error.localizedMessage ?: "network unavailable"}"
                            )
                        } else {
                            state.copy(isLoading = false, error = error.localizedMessage)
                        }
                    }
                )
            }
        }
    }

    fun updateSourceLanguage(language: VavusLanguage) {
        _uiState.update { it.copy(sourceLanguage = language) }
    }

    fun updateTargetLanguage(language: VavusLanguage) {
        _uiState.update { it.copy(targetLanguage = language) }
    }

    fun updateText(value: String) {
        _uiState.update { it.copy(textToTranslate = value) }
    }

    fun translate() {
        val state = _uiState.value
        if (state.sourceLanguage == null || state.targetLanguage == null || state.textToTranslate.isBlank()) {
            _uiState.update { it.copy(error = "Please choose languages and enter text.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isTranslating = true, error = null) }
            val result = repository.translate(
                sourceLanguage = state.sourceLanguage.code,
                targetLanguage = state.targetLanguage.code,
                text = state.textToTranslate
            )
            _uiState.update {
                result.fold(
                    onSuccess = { translation ->
                        it.copy(
                            isTranslating = false,
                            translatedText = translation,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        it.copy(isTranslating = false, error = error.localizedMessage)
                    }
                )
            }
        }
    }

    fun swapLanguages() {
        _uiState.update {
            it.copy(
                sourceLanguage = it.targetLanguage,
                targetLanguage = it.sourceLanguage,
                translatedText = ""
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update { TranslatorUiState() }
        }
    }
}

data class TranslatorUiState(
    val username: String = "",
    val languages: List<VavusLanguage> = emptyList(),
    val sourceLanguage: VavusLanguage? = null,
    val targetLanguage: VavusLanguage? = null,
    val textToTranslate: String = "",
    val translatedText: String = "",
    val isLoading: Boolean = false,
    val isTranslating: Boolean = false,
    val error: String? = null
)