package com.example.horsetracker.presentation

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aitracker.api.DetectionBox
import com.example.aitracker.api.Detector
import com.example.aitracker.api.DetectorFactory
import com.example.horsetracker.presentation.util.CameraController
import com.example.horsetracker.presentation.feature.camera.CameraPermissionCheckSnackbar
import com.example.horsetracker.presentation.feature.camera.CameraViewModel
import com.example.horsetracker.presentation.util.CameraXController
import com.example.horsetracker.presentation.theme.HorseTrackerTheme

class MainActivity : ComponentActivity(), Detector.DetectionListener,
    CameraController.CameraFrameProcessor {

    private lateinit var cameraViewModel: CameraViewModel
    private val horseDetector: Detector by lazy {
        DetectorFactory.createHorseDetector(
            context = baseContext,
            listener = this
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

    override fun detectionResult(results: List<DetectionBox>) {
        cameraViewModel.updateBoundingBoxes(results)
    }

    override fun emptyDetection() {
        cameraViewModel.updateBoundingBoxes(emptyList())
    }

    override fun detectionError(e: Exception) {
        cameraViewModel.updateBoundingBoxes(emptyList())
    }

    override fun processFrame(frame: Bitmap) {
        horseDetector.detect(frame)
    }

    override fun processError(exception: Exception) {
        cameraViewModel.updateBoundingBoxes(emptyList())
    }
}


