package com.example.horsetracker.presentation.model

import com.example.horsetracker.domain.model.DetectionObject

fun DetectionObject.toUiState(): AiDetectionBox {
    return AiDetectionBox(
        id = this.id,
        label = this.label,
        left = this.boundingBox.left,
        right = this.boundingBox.right,
        top = this.boundingBox.top,
        bottom = this.boundingBox.bottom,
        confidence = this.boundingBox.confidence
    )
}