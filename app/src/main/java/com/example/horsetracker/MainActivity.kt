package com.example.horsetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CameraScreenWithPermission()
        }
    }
}

@Composable
fun CameraScreenWithPermission() {
    var hasCameraPermission by remember { mutableStateOf(false) }

    if (!hasCameraPermission) {
        // 권한 요청
        RequestCameraPermission(
            onPermissionGranted = { hasCameraPermission = true },
            onPermissionDenied = { /* TODO: 추후 재요청 or UI 처리 */ }
        )
    } else {
        // 권한이 허용되었다면 카메라 화면 표시
        CameraScreen()
    }
}