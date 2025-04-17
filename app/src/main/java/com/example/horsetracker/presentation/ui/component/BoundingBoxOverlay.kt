package com.example.horsetracker.presentation.ui.component

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import com.example.horsetracker.presentation.detector.BoundingBox

@Composable
fun BoundingBoxOverlay(
    modifier: Modifier = Modifier,
    boundingBoxes: List<BoundingBox>
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

        boundingBoxes.forEach { box ->
            val left = box.x1 * size.width
            val top = box.y1 * size.height
            val right = box.x2 * size.width
            val bottom = box.y2 * size.height

            drawContext.canvas.nativeCanvas.drawRect(left, top, right, bottom, boxPaint)

            textPaint.getTextBounds(box.clsName, 0, box.clsName.length, bounds)
            val textHeight = bounds.height().toFloat()

            drawContext.canvas.nativeCanvas.drawText(
                box.clsName,
                left,
                bottom + textHeight,
                textPaint
            )
        }
    }
}