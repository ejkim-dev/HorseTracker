package com.example.aitracker.api

import android.graphics.Bitmap

interface Detector {
    fun setup()
    fun detect(image: Bitmap)
    fun clear()

    interface DetectionListener {
        fun detectionResult(results: List<DetectionBox>)
        fun emptyDetection()
        fun detectionError(e: Exception)
    }
}