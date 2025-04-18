package com.example.horsetracker.presentation.util

import android.graphics.Bitmap
import androidx.camera.view.PreviewView

interface CameraController {
    fun setupCamera()
    fun shutdown()
    fun setViewFinder(previewView: PreviewView)

    interface CameraFrameProcessor {
        fun processFrame(frame: Bitmap)
        fun processError(exception: Exception)
    }
}