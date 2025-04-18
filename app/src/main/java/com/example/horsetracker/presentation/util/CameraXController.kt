package com.example.horsetracker.presentation.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraXController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val cameraFrameProcessor: CameraController.CameraFrameProcessor,
    private val isFrontCamera: Boolean = false
): CameraController {

    private var viewFinder: PreviewView? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    override fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindUseCases()
        }, ContextCompat.getMainExecutor(context))
    }

    override fun shutdown() {
        cameraExecutor.shutdown()
    }

    override fun setViewFinder(previewView: PreviewView) {
        viewFinder = previewView
    }

    private fun bindUseCases() {
        val provider = cameraProvider ?: return

        val preview = Preview.Builder()
            .build().apply {
                surfaceProvider = viewFinder?.surfaceProvider
            }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build().apply {
                setAnalyzer(cameraExecutor) { imageProxy ->
                    val bitmap = convertToBitmap(imageProxy)
                    cameraFrameProcessor.processFrame(bitmap)
                    imageProxy.close()
                }
            }

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(
                if (isFrontCamera) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            ).build()

        try {
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalyzer)
        } catch (e: Exception) {
            cameraFrameProcessor.processError(e)
        }
    }

    private fun convertToBitmap(imageProxy: ImageProxy): Bitmap {
        val buffer = imageProxy.planes[0].buffer
        val bitmap = Bitmap.createBitmap(imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)

        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            if (isFrontCamera) postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}