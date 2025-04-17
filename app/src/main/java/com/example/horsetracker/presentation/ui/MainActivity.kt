package com.example.horsetracker.presentation.ui

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.horsetracker.presentation.detector.BoundingBox
import com.example.horsetracker.presentation.detector.Detector
import com.example.horsetracker.presentation.ui.camera.CameraController
import com.example.horsetracker.presentation.ui.camera.CameraPermissionCheckSnackbar
import com.example.horsetracker.presentation.ui.camera.CameraViewModel
import com.example.horsetracker.presentation.ui.camera.CameraXController
import com.example.horsetracker.presentation.ui.theme.HorseTrackerTheme

class MainActivity : ComponentActivity(), Detector.DetectorListener,
    CameraController.CameraFrameProcessor {

    private lateinit var cameraViewModel: CameraViewModel
    private val horseDetector: Detector by lazy {
        Detector(
            baseContext,
            MODEL_PATH,
            listOf("horse"),
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        horseDetector.setup()

        setContent {
            cameraViewModel = viewModel()

            LaunchedEffect(Unit) {
                cameraViewModel.apply {
                    initCameraController(
                        CameraXController(
                            context = baseContext,
                            lifecycleOwner = this@MainActivity,
                            cameraFrameProcessor = this@MainActivity
                        )
                    )
                }
            }

            HorseTrackerTheme(
                darkTheme = true,
            ) {
                Surface {
                    CameraPermissionCheckSnackbar(
                        viewModel = cameraViewModel
                    )
                }
            }
        }
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>) {
        cameraViewModel.updateBoundingBoxes(boundingBoxes)
    }

    override fun onEmptyDetect() {
        cameraViewModel.updateBoundingBoxes(emptyList())
    }

    override fun onDetectError(e: Exception) {
        cameraViewModel.updateBoundingBoxes(emptyList())
    }

    override fun processFrame(frame: Bitmap) {
        horseDetector.detect(frame)
    }

    override fun onError(exception: Exception) {
        cameraViewModel.updateBoundingBoxes(emptyList())
    }

    companion object {
        private const val MODEL_PATH = "best_float32.tflite"
    }
}


