package com.ps.perfectpermissionsmvi

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.ps.perfectpermissionsmvi.ui.theme.PerfectPermissionsMVITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel by viewModels<MainViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            val lifecycleOwner = LocalLifecycleOwner.current

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                viewModel.onPermissionResult(
                    isGranted = isGranted,
                    shouldShowRationale = shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                )
            }

            PerfectPermissionsMVITheme {

                LaunchedEffect(lifecycleOwner) {
                    lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
                        permissionLauncher.launch(input = Manifest.permission.CAMERA)
                    }
                }

                LaunchedEffect(Unit) {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            UiEvent.OpenAppSettings -> openAppSettings()
                            UiEvent.RequestCameraPermission -> {
                                permissionLauncher.launch(input = Manifest.permission.CAMERA)
                            }
                        }
                    }
                }


                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = uiState.permissionText)

                        if (uiState.permissionPermanentlyDenied) {
                            Button(onClick = { viewModel.onEvent(event = UserEvent.OpenSettingsClicked) }) {
                                Text(text = "Open Settings")
                            }
                        }

                        if (uiState.isRationaleVisible) {
                            Dialog(onDismissRequest = {
                                viewModel.onEvent(UserEvent.RationaleButtonClicked)
                            }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "This is a rationale")
                                    Button(onClick = {
                                        viewModel.onEvent(UserEvent.RationaleButtonClicked)
                                    }) {
                                        Text("OK")
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}

data class UiState(
    val isRationaleVisible: Boolean = false,
    val permissionText: String = "Permission Idle",
    val permissionPermanentlyDenied: Boolean = false,
)

sealed interface UiEvent {
    data object OpenAppSettings : UiEvent
    data object RequestCameraPermission : UiEvent
}

sealed interface UserEvent {
    data object RationaleButtonClicked : UserEvent
    data object OpenSettingsClicked : UserEvent
}

fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}

