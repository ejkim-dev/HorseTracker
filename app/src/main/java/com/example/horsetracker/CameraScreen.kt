package com.example.horsetracker

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraScreen() {
    val localContext = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraManager = remember { CameraManager(localContext, lifecycleOwner) }
    val lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }

    // 카메라가 준비되었는지 여부
    var isCameraInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cameraManager.initializeCamera(
            lensFacing = lensFacing,
            onInitialized = {
                isCameraInitialized = true
            },
            onError = { e ->
                Log.e("CameraScreen", "Error initializing camera: $e")
            }
        )
    }

    Box {
        // 카메라 프리뷰 초기화 후 보여짐
        if (isCameraInitialized) {
            CameraPreview(cameraManager)
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
        ) {
            Button(
                onClick = {
                    cameraManager.takePicture(
                        onImageSaved = {
                            Log.d("TEMP", "CameraScreen: $it")
                        },
                        onError = {
                            Log.e("TEMP", "CameraScreen: $it")
                        }
                    )
                }
            ) {
                Text("Take Photo")
            }
        }
    }
}

@Composable
private fun CameraPreview(cameraManager: CameraManager) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            PreviewView(context).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                cameraManager.setPreviewSurface(this.surfaceProvider)
            }
        },
        update = { view ->
            cameraManager.setPreviewSurface(view.surfaceProvider)
        }
    )
}