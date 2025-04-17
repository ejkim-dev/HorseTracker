package com.example.horsetracker.presentation.ui.camera

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.aitracker.api.DetectionBox
import com.example.horsetracker.presentation.ui.camera.uistate.CameraState

class CameraViewModel: ViewModel() {
    private val _cameraState = mutableStateOf<CameraState>(CameraState.PermissionDenied)
    val cameraState: State<CameraState> = _cameraState

    private var _cameraController: CameraController? = null
    val cameraController: CameraController
        get() = _cameraController ?: throw IllegalStateException("Camera not initialized")


    private val _boundingBoxes = mutableStateOf<List<DetectionBox>>(emptyList())
    val boundingBoxes: State<List<DetectionBox>> = _boundingBoxes

    override fun onCleared() {
        super.onCleared()
        _cameraController?.shutdown()
    }

    fun initCameraController(cameraController: CameraController) {
        if (_cameraController == null) {
            _cameraController = cameraController
        }
    }

    fun updateBoundingBoxes(newBoxes: List<DetectionBox>) {
        _boundingBoxes.value = newBoxes
    }

    fun onPermissionResult(isGranted: Boolean) {
        _cameraState.value = if (isGranted) {
            CameraState.ReadyToStartCamera
        } else {
            CameraState.PermissionDenied
        }
    }

    fun onCameraInitialized() {
        _cameraState.value = CameraState.PermissionGranted
    }
}