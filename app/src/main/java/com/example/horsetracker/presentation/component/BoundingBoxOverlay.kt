package com.example.horsetracker.presentation.component

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import com.example.horsetracker.presentation.model.AiDetectionBox

@Composable
fun BoundingBoxOverlay(
    modifier: Modifier = Modifier,
    detectionBox: List<AiDetectionBox>
) {
    Canvas(modifier = modifier) {
        val boxPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        val textPaint = Paint().apply {
            color = Color.CYAN
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
        }

        val bounds = Rect()

        detectionBox.forEach { box ->
            val left = box.left * size.width
            val top = box.top * size.height
            val right = box.right * size.width
            val bottom = box.bottom * size.height

            drawContext.canvas.nativeCanvas.drawRect(left, top, right, bottom, boxPaint)

            textPaint.getTextBounds(box.label, 0, box.label.length, bounds)
            val textHeight = bounds.height().toFloat()

            drawContext.canvas.nativeCanvas.drawText(
                "${box.label} (${(box.confidence * 100).toInt()}%)", // ui에서 정확도 확인을 위함
                left,
                bottom + textHeight,
                textPaint
            )
        }
    }
}