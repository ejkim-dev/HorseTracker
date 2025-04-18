package com.example.aitracker.core

import com.example.aitracker.api.DetectionBox

internal data class BoundingBox(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val cx: Float,
    val cy: Float,
    val w: Float,
    val h: Float,
    val cnf: Float,
    val cls: Int,
    val clsName: String
) {
    fun toDetectionBox(): DetectionBox {
        return DetectionBox(
            left = x1,
            top = y1,
            right = x2,
            bottom = y2,
            label = clsName,
            confidence = cnf
        )
    }
}