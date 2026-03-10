package com.example.cursy.features.settings.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.core.di.AuthManager
import com.example.cursy.core.network.CoursyApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val showLogoutDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val navigateToLogin: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val coursyApi: CoursyApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun showLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    fun hideLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun logout() {
        authManager.clear()
        _uiState.update { it.copy(navigateToLogin = true) }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                coursyApi.deleteAccount()
                authManager.clear()
                _uiState.update { it.copy(navigateToLogin = true) }
            } catch (e: Exception) {
                Log.e("Settings", "Error al eliminar cuenta: ${e.message}")
            }
        }
    }

    fun resetNavigation() {
        _uiState.update { it.copy(navigateToLogin = false) }
    }
}
