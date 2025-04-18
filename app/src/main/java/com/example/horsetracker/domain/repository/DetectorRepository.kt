package com.example.horsetracker.domain.repository

import android.graphics.Bitmap
import com.example.horsetracker.domain.model.DetectionObject
import kotlinx.coroutines.flow.Flow

interface DetectorRepository {
    fun getDetectionResults(): Flow<List<DetectionObject>>
    fun detectObjects(image: Bitmap)
    fun setup()
    fun release()
}