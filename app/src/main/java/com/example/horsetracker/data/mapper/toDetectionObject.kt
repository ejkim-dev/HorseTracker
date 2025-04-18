package com.example.horsetracker.data.mapper

import com.example.aitracker.api.DetectionBox
import com.example.horsetracker.domain.model.BoundingBox
import com.example.horsetracker.domain.model.DetectionObject
import kotlin.math.absoluteValue

fun DetectionBox.toDetectionObject(): DetectionObject {
    val hashComponents = listOf(label, left, top, right, bottom)
    val hashId = hashComponents.joinToString("").hashCode()

    return DetectionObject(
        id = hashId.absoluteValue, // 음수 방지를 위해 절대값 사용
        label = this.label,
        boundingBox = BoundingBox(
            left = this.left,
            top = this.top,
            right = this.right,
            bottom = this.bottom,
            confidence = this.confidence
        )
    )
}