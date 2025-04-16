package com.example.horsetracker.presentation.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.horsetracker.R
import com.example.horsetracker.presentation.detector.BoundingBox
import com.example.horsetracker.presentation.detector.Detector
import com.example.horsetracker.presentation.ui.theme.HorseTrackerTheme
import com.example.horsetracker.presentation.ui.ai.ModelImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), Detector.DetectorListener  {
    private var sampleImage by mutableStateOf<Bitmap?>(null)
    private lateinit var horseDetector: Detector
    private val processingScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sampleImage = BitmapFactory.decodeResource(resources, R.drawable.sample)
        horseDetector = Detector(baseContext, MODEL_PATH, listOf("horse"), this).apply {
            setup()
        }
        sampleImage?.let { horseDetector.detect(it) }

        setContent {
            HorseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ModelImage(bitmap = sampleImage)
                }
            }
        }
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>) {
        sampleImage?.let { bmp ->
            processingScope.launch {
                val updatedBitmap = drawBoundingBoxes(bmp, boundingBoxes)
                sampleImage = updatedBitmap
            }
        }
    }

    override fun onEmptyDetect() {
        Log.i(TAG, "empty")
    }

    override fun onDetectError(e: Exception) {
        Log.e(TAG, "${e.message}")
    }

    private fun drawBoundingBoxes(bitmap: Bitmap, boxes: List<BoundingBox>): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 20f
        }
        val textPaint = Paint().apply {
            color = Color.rgb(255,255,255)
            textSize = 150f
            typeface = Typeface.DEFAULT_BOLD
        }

        for (box in boxes) {
            val rect = RectF(
                box.x1 * mutableBitmap.width,
                box.y1 * mutableBitmap.height,
                box.x2 * mutableBitmap.width,
                box.y2 * mutableBitmap.height
            )
            canvas.drawRect(rect, paint)
            canvas.drawText(box.clsName, rect.left, rect.bottom, textPaint)
        }

        return mutableBitmap
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val MODEL_PATH = "best_float32.tflite"
    }
}


