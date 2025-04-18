package com.example.horsetracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.horsetracker.di.ServiceLocator
import com.example.horsetracker.presentation.feature.camera.CameraPermissionCheckSnackbar
import com.example.horsetracker.presentation.feature.camera.CameraViewModel
import com.example.horsetracker.presentation.theme.HorseTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = ServiceLocator.provideCameraViewModelFactory(applicationContext)

        setContent {
            val cameraViewModel: CameraViewModel = viewModel(factory = factory)

            LaunchedEffect(Unit) {
                cameraViewModel.initialize(
                    context = baseContext,
                    lifecycleOwner = this@MainActivity
                )
            }

            HorseTrackerTheme(
                darkTheme = true,
            ) {
                Surface {
                    CameraPermissionCheckSnackbar(
                        viewModel = cameraViewModel
                    )
                }
            }
        }
    }
}


