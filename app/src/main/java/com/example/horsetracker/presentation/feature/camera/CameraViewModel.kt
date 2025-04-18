package com.example.horsetracker.presentation.feature.camera

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.example.aitracker.api.DetectionBox
import com.example.aitracker.api.Detector
import com.example.aitracker.api.DetectorFactory
import com.example.horsetracker.presentation.feature.uistate.CameraState
import com.example.horsetracker.presentation.util.CameraController
import com.example.horsetracker.presentation.util.CameraXController

class CameraViewModel: ViewModel(), Detector.DetectionListener, CameraController.CameraFrameProcessor  {
    private val _cameraState = mutableStateOf<CameraState>(CameraState.PermissionDenied)
    val cameraState: State<CameraState> = _cameraState

    private var _cameraController: CameraController? = null
    val cameraController: CameraController
        get() = _cameraController ?: throw IllegalStateException("Camera not initialized")

    private val _boundingBoxes = mutableStateOf<List<DetectionBox>>(emptyList())
    val boundingBoxes: State<List<DetectionBox>> = _boundingBoxes

    private lateinit var horseDetector: Detector

    override fun onCleared() {
        super.onCleared()
        _cameraController?.shutdown()
        if (::horseDetector.isInitialized) {
            horseDetector.clear()
        }
    }

    override fun detectionResult(results: List<DetectionBox>) {
        updateBoundingBoxes(results)
    }

    override fun emptyDetection() {
        updateBoundingBoxes(emptyList())
    }

    override fun detectionError(e: Exception) {
        updateBoundingBoxes(emptyList())
    }

    override fun processFrame(frame: Bitmap) {
        horseDetector.detect(frame)
    }

    override fun processError(exception: Exception) {
        updateBoundingBoxes(emptyList())
    }

    // 카메라와 Detector 설정
    fun initialize(context: Context, lifecycleOwner: LifecycleOwner) {
        // Detector 초기화
        horseDetector = DetectorFactory.createHorseDetector(
            context = context,
            listener = this
        )
        horseDetector.setup()

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

    private fun updateBoundingBoxes(newBoxes: List<DetectionBox>) {
        _boundingBoxes.value = newBoxes
    }
}