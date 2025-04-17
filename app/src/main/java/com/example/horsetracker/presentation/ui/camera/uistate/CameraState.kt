package com.example.horsetracker.presentation.ui.camera.uistate

sealed class CameraState{
    object PermissionDenied : CameraState()
    object PermissionGranted : CameraState()
    object ReadyToStartCamera : CameraState()
}