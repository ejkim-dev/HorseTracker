package com.example.horsetracker.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.aitracker.api.DetectionBox
import com.example.aitracker.api.Detector
import com.example.aitracker.api.DetectorFactory
import com.example.horsetracker.data.mapper.toDetectionObject
import com.example.horsetracker.domain.model.DetectionObject
import com.example.horsetracker.domain.repository.DetectorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class DetectorRepositoryImpl(
    private val context: Context
) : DetectorRepository, Detector.DetectionListener {

    private val detector: Detector by lazy {
        DetectorFactory.createHorseDetector(context, this)
    }
    
    private val _detectionResults = MutableSharedFlow<List<DetectionObject>>(extraBufferCapacity = 1)
    
    override fun getDetectionResults(): Flow<List<DetectionObject>> = _detectionResults.asSharedFlow()
    
    override fun detectObjects(image: Bitmap) {
        detector.detect(image)
    }
    
    override fun setup() {
        detector.setup()
    }
    
    override fun release() {
        detector.clear()
    }
    
    /**
     *  Detector.DetectionListener 구현
     *  */
    override fun detectionResult(results: List<DetectionBox>) {
        val mappedResults = results.map { it.toDetectionObject() }
        _detectionResults.tryEmit(mappedResults)
    }
    
    override fun emptyDetection() {
        _detectionResults.tryEmit(emptyList())
    }
    
    override fun detectionError(e: Exception) {
        _detectionResults.tryEmit(emptyList())
    }
}
