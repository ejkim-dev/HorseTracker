package com.example.aitracker.api

import android.content.Context
import com.example.aitracker.core.TFLiteDetector

object DetectorFactory {
    fun createHorseDetector(
        context: Context,
        listener: Detector.DetectionListener?
    ): Detector {
        val modelPath = "best_float32.tflite"
        val labels = listOf("horse")
        return TFLiteDetector(context, listener, modelPath, labels)
    }
}