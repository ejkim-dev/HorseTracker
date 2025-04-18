package com.example.horsetracker.domain.model

data class DetectionObject(
    val id: Int,
    val label: String,
    val boundingBox: BoundingBox
)

data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val confidence: Float
)