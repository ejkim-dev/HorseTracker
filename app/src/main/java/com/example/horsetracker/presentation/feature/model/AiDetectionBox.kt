package com.example.horsetracker.presentation.feature.model

data class AiDetectionBox(
    val id: Int,
    val label: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val confidence: Float
)
