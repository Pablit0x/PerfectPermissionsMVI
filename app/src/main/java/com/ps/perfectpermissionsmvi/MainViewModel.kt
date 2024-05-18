package com.ps.perfectpermissionsmvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()


    fun onEvent(event: UserEvent) {
        when (event) {
            UserEvent.OpenSettingsClicked -> emitEvent(event = UiEvent.OpenAppSettings)
            UserEvent.RationaleButtonClicked -> {
                _uiState.update { it.copy(isRationaleVisible = false) }
                emitEvent(event = UiEvent.RequestCameraPermission)
            }
        }
    }

    private fun emitEvent(event: UiEvent) = viewModelScope.launch { _uiEvent.emit(value = event) }

    fun onPermissionResult(isGranted: Boolean, shouldShowRationale: Boolean) {
        if (isGranted) _uiState.update {
            it.copy(
                permissionText = "Permission granted", permissionPermanentlyDenied = false
            )
        }
        else if (shouldShowRationale) _uiState.update {
            it.copy(
                isRationaleVisible = true, permissionText = "", permissionPermanentlyDenied = false
            )
        }
        else _uiState.update {
            it.copy(
                isRationaleVisible = false,
                permissionPermanentlyDenied = true,
                permissionText = "Permission permanently denied"
            )
        }
    }
}