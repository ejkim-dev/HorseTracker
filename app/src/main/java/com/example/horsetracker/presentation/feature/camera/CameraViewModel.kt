package com.example.horsetracker.presentation.feature.camera

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.horsetracker.domain.usecase.DetectObjectsUseCase
import com.example.horsetracker.presentation.feature.model.AiDetectionBox
import com.example.horsetracker.presentation.feature.model.CameraState
import com.example.horsetracker.presentation.feature.model.toUiState
import com.example.horsetracker.presentation.util.CameraController
import com.example.horsetracker.presentation.util.CameraXController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class CameraViewModel(
    private val detectObjectsUseCase: DetectObjectsUseCase
): ViewModel(), CameraController.CameraFrameProcessor  {
    private val _cameraState = mutableStateOf<CameraState>(CameraState.PermissionDenied)
    val cameraState: State<CameraState> = _cameraState

    private var _cameraController: CameraController? = null
    val cameraController: CameraController
        get() = _cameraController ?: throw IllegalStateException("Camera not initialized")

    private val _boundingBoxes = mutableStateOf<List<AiDetectionBox>>(emptyList())
    val boundingBoxes: State<List<AiDetectionBox>> = _boundingBoxes

    init {
        // 탐지 결과 수신 및 UI 모델로 변환
        detectObjectsUseCase.getDetectionResults()
            .map { detections -> detections.map { it.toUiState() } }
            .onEach { boxes -> _boundingBoxes.value = boxes }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        _cameraController?.shutdown()
        detectObjectsUseCase.release()
    }

    override fun processFrame(frame: Bitmap) {
        detectObjectsUseCase.detectObjects(frame)
    }

    override fun processError(exception: Exception) {
        updateBoundingBoxes(emptyList())
    }

    // 카메라와 Detector 설정
    fun initialize(context: Context, lifecycleOwner: LifecycleOwner) {
        // Detector 초기화
        detectObjectsUseCase.setup()

        // CameraController 초기화
        initCameraController(
            CameraXController(
                context = context,
                lifecycleOwner = lifecycleOwner,
                cameraFrameProcessor = this
            )
        )
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

    private fun initCameraController(cameraController: CameraController) {
        if (_cameraController == null) {
            _cameraController = cameraController
        }
    }

    private fun updateBoundingBoxes(newBoxes: List<AiDetectionBox>) {
        _boundingBoxes.value = newBoxes
    }
}