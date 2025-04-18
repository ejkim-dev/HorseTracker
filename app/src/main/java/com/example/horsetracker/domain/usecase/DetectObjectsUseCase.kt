package com.example.horsetracker.domain.usecase

import android.graphics.Bitmap
import com.example.horsetracker.domain.model.DetectionObject
import com.example.horsetracker.domain.repository.DetectorRepository
import kotlinx.coroutines.flow.Flow

class DetectObjectsUseCase(
    private val detectorRepository: DetectorRepository
) {
    
    fun getDetectionResults(): Flow<List<DetectionObject>> {
        return detectorRepository.getDetectionResults()
    }
    
    fun detectObjects(image: Bitmap) {
        detectorRepository.detectObjects(image)
    }
    
    fun setup() {
        detectorRepository.setup()
    }
    
    fun release() {
        detectorRepository.release()
    }
}