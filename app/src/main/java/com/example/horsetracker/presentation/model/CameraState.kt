package com.example.horsetracker.presentation.model

sealed class CameraState{
    object PermissionDenied : CameraState()
    object PermissionGranted : CameraState()
    object ReadyToStartCamera : CameraState()
}