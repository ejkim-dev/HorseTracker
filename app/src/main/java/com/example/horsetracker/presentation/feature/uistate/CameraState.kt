package com.example.horsetracker.presentation.feature.uistate

sealed class CameraState{
    object PermissionDenied : CameraState()
    object PermissionGranted : CameraState()
    object ReadyToStartCamera : CameraState()
}