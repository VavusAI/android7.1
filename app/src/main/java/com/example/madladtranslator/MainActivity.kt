package com.example.madladtranslator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.madladtranslator.ui.AppViewModelFactory
import com.example.madladtranslator.ui.AuthViewModel
import com.example.madladtranslator.ui.TranslatorViewModel
import com.example.madladtranslator.ui.screens.CreateAccountScreen
import com.example.madladtranslator.ui.screens.LoginScreen
import com.example.madladtranslator.ui.screens.TranslatorScreen
import com.example.madladtranslator.ui.theme.MadladTranslatorTheme
import kotlinx.coroutines.launch

private const val ROUTE_LOGIN = "login"
private const val ROUTE_REGISTER = "register"
private const val ROUTE_TRANSLATOR = "translator"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as MadladApp).container
        val viewModelFactory = AppViewModelFactory(appContainer.repository, appContainer.session)

        setContent {
            MadladTranslatorTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                val translatorViewModel: TranslatorViewModel = viewModel(factory = viewModelFactory)

                val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
                val translatorUiState by translatorViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(authUiState.isLoggedIn) {
                    val currentRoute = navController.currentDestination?.route
                    if (authUiState.isLoggedIn) {
                        if (currentRoute != ROUTE_TRANSLATOR) {
                            navController.navigate(ROUTE_TRANSLATOR) {
                                popUpTo(ROUTE_LOGIN) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        translatorViewModel.refreshLanguages()
                    } else if (currentRoute != ROUTE_LOGIN) {
                        navController.navigate(ROUTE_LOGIN) {
                            popUpTo(ROUTE_LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                LaunchedEffect(authUiState.error) {
                    authUiState.error?.let { message ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                            authViewModel.clearError()
                        }
                    }
                }

                LaunchedEffect(translatorUiState.error) {
                    translatorUiState.error?.let { message ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(text = stringResource(id = R.string.app_name)) },
                            colors = TopAppBarDefaults.topAppBarColors()
                        )
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_LOGIN,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(ROUTE_LOGIN) {
                            LoginScreen(
                                uiState = authUiState,
                                onBaseUrlChanged = authViewModel::updateBaseUrl,
                                onUsernameChanged = authViewModel::updateUsername,
                                onPasswordChanged = authViewModel::updatePassword,
                                onLogin = authViewModel::login,
                                onNavigateToRegister = {
                                    navController.navigate(ROUTE_REGISTER)
                                }
                            )
                        }
                        composable(ROUTE_REGISTER) {
                            CreateAccountScreen(
                                uiState = authUiState,
                                onBaseUrlChanged = authViewModel::updateBaseUrl,
                                onUsernameChanged = authViewModel::updateUsername,
                                onPasswordChanged = authViewModel::updatePassword,
                                onOrderNumberChanged = authViewModel::updateOrderNumber,
                                onCreateAccount = authViewModel::register,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(ROUTE_TRANSLATOR) {
                            if (authUiState.isLoggedIn) {
                                TranslatorScreen(
                                    uiState = translatorUiState,
                                    onRefreshLanguages = translatorViewModel::refreshLanguages,
                                    onSourceLanguageSelected = translatorViewModel::updateSourceLanguage,
                                    onTargetLanguageSelected = translatorViewModel::updateTargetLanguage,
                                    onSwapLanguages = translatorViewModel::swapLanguages,
                                    onTextChanged = translatorViewModel::updateText,
                                    onTranslate = translatorViewModel::translate,
                                    onLogout = translatorViewModel::logout
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}