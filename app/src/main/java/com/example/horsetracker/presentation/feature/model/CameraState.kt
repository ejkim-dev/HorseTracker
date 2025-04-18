package com.example.horsetracker.presentation.feature.model

sealed class CameraState{
    object PermissionDenied : CameraState()
    object PermissionGranted : CameraState()
    object ReadyToStartCamera : CameraState()
}