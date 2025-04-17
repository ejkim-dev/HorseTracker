package com.example.horsetracker.presentation.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.example.horsetracker.presentation.detector.BoundingBox
import com.example.horsetracker.presentation.ui.camera.uistate.CameraState
import com.example.horsetracker.presentation.ui.component.BoundingBoxOverlay
import com.example.horsetracker.presentation.ui.component.RequestPermissions
import kotlinx.coroutines.launch

@Composable
fun AiCameraScreen(
    controller: CameraController,
    boundingBoxes: List<BoundingBox>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clipToBounds()
    ) {
        CameraScreen(controller = controller)

        BoundingBoxOverlay(
            boundingBoxes = boundingBoxes,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun CameraScreen(
    controller: CameraController
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE

                controller.setViewFinder(this)
                controller.setupCamera()
            }
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CameraPermissionCheckSnackbar(
    viewModel: CameraViewModel
) {
    val cameraState = viewModel.cameraState.value
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(cameraState) {
        if (cameraState == CameraState.ReadyToStartCamera) {
            viewModel.onCameraInitialized()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackBarHostState) }) {
        when (cameraState) {
            is CameraState.PermissionDenied -> {
                RequestPermissions(
                    permissions = listOf(Manifest.permission.CAMERA),
                    onResult = { results ->
                        val isGranted = results[Manifest.permission.CAMERA] == true
                        viewModel.onPermissionResult(isGranted)

                        if (!isGranted) {
                            scope.launch {
                                snackBarHostState.showSnackbar(message = "카메라 권한이 거부되었습니다.")
                            }
                        }
                    }
                )
            }

            is CameraState.PermissionGranted -> {
                AiCameraScreen(
                    controller = viewModel.cameraController,
                    boundingBoxes = viewModel.boundingBoxes.value
                )
            }

            else -> { /* Loading UI or empty box */ }
        }
    }
}