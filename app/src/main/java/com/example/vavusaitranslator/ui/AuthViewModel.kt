package com.example.vavusaitranslator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vavusaitranslator.data.VavusRepository
import com.example.vavusaitranslator.data.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: VavusRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            session.authToken.collectLatest { token ->
                _uiState.update { it.copy(isLoggedIn = !token.isNullOrBlank()) }
            }
        }
        viewModelScope.launch {
            session.username.collectLatest { username ->
                if (!username.isNullOrBlank()) {
                    _uiState.update { it.copy(username = username) }
                }
            }
        }
    }

    fun updateUsername(value: String) {
        _uiState.update { it.copy(username = value.trim()) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun updateOrderNumber(value: String) {
        _uiState.update { it.copy(orderNumber = value.trim()) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Please provide a username and password.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repository.login(state.username, state.password)
            _uiState.update {
                it.copy(
                    loading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun register() {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank() || state.orderNumber.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in every field, including the order number.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repository.register(state.username, state.password, state.orderNumber)
            _uiState.update {
                it.copy(
                    loading = false,
                    error = result.exceptionOrNull()?.localizedMessage,
                    orderNumber = if (result.isSuccess) "" else state.orderNumber
                )
            }
        }
    }
}

data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val orderNumber: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)