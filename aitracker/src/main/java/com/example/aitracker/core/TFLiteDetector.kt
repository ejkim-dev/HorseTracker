package com.example.aitracker.core

import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import android.content.Context
import android.graphics.Bitmap
import com.example.aitracker.api.Detector
import java.nio.ByteBuffer

internal class TFLiteDetector(
    private val context: Context,
    private val modelPath: String,
    private val labels: List<String>,
    private val listener: Detector.DetectionListener
): Detector {
    private var interpreter: Interpreter? = null
    private var tracked: BoundingBox? = null
    private var tensorWidth = 0
    private var tensorHeight = 0
    private var numChannels = 0
    private var numElements = 0

    private val imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(INPUT_MEAN, INPUT_STD))
        .add(CastOp(INPUT_TYPE))
        .build()

    override fun setup() {
        val model = FileUtil.loadMappedFile(context, modelPath)
        val options = Interpreter.Options().apply { numThreads = 4 }
        interpreter = Interpreter(model, options)

        interpreter?.getInputTensor(0)?.shape()?.let {
            tensorWidth = it[1]; tensorHeight = it[2]
        }
        interpreter?.getOutputTensor(0)?.shape()?.let {
            numChannels = it[1]; numElements = it[2]
        }
    }

    override fun detect(image: Bitmap) {
        try {
            val input = preprocess(image)
            val output = TensorBuffer.createFixedSize(
                intArrayOf(1, numChannels, numElements),
                OUTPUT_TYPE
            )
            interpreter?.run(input, output.buffer)
            val boxes = parseOutput(output.floatArray)

            if (boxes.isNullOrEmpty()) {
                tracked = null
                listener.emptyDetection()
            } else {
                tracked = track(boxes)
                listener.detectionResult(listOfNotNull(tracked?.toDetectionBox()))
            }
        } catch (e: Exception) {
            listener.detectionError(e)
        }
    }

    override fun clear() {
        interpreter?.close()
        interpreter = null
    }

    private fun preprocess(image: Bitmap): ByteBuffer {
        val scaled = Bitmap.createScaledBitmap(image, tensorWidth, tensorHeight, false)
        val tensor = TensorImage(INPUT_TYPE).apply { load(scaled) }
        return imageProcessor.process(tensor).buffer
    }

    private fun parseOutput(array: FloatArray): List<BoundingBox>? {
        val boxes = mutableListOf<BoundingBox>()

        for (i in 0 until numElements) {
            var maxConf = -1f
            var maxIdx = -1

            for (j in 4 until numChannels) {
                val conf = array[i + j * numElements]
                if (conf > maxConf) {
                    maxConf = conf
                    maxIdx = j - 4
                }
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                val cx = array[i]
                val cy = array[i + numElements]
                val w = array[i + numElements * 2]
                val h = array[i + numElements * 3]
                val x1 = cx - w / 2f
                val y1 = cy - h / 2f
                val x2 = cx + w / 2f
                val y2 = cy + h / 2f

                if (x1 in 0f..1f && x2 in 0f..1f && y1 in 0f..1f && y2 in 0f..1f) {
                    boxes.add(BoundingBox(x1, y1, x2, y2, cx, cy, w, h, maxConf, maxIdx, labels[maxIdx]))
                }
            }
        }

        return boxes.takeIf { it.isNotEmpty() }?.let { applyNMS(it) }
    }

    private fun track(boxes: List<BoundingBox>): BoundingBox? {
        return if (tracked == null) {
            boxes.maxByOrNull { it.cnf }
        } else {
            val newBox = boxes.maxByOrNull { calculateIoU(tracked!!, it) }
            if (newBox != null && calculateIoU(tracked!!, newBox) > 0.3f) newBox else null
        }
    }

    private fun applyNMS(boxes: List<BoundingBox>): List<BoundingBox> {
        val sorted = boxes.sortedByDescending { it.cnf }.toMutableList()
        val result = mutableListOf<BoundingBox>()

        while (sorted.isNotEmpty()) {
            val first = sorted.removeAt(0)
            result.add(first)
            sorted.removeAll { calculateIoU(first, it) >= IOU_THRESHOLD }
        }
        return result
    }

    private fun calculateIoU(a: BoundingBox, b: BoundingBox): Float {
        val x1 = maxOf(a.x1, b.x1)
        val y1 = maxOf(a.y1, b.y1)
        val x2 = minOf(a.x2, b.x2)
        val y2 = minOf(a.y2, b.y2)

        val interArea = maxOf(0f, x2 - x1) * maxOf(0f, y2 - y1)
        val unionArea = a.w * a.h + b.w * b.h - interArea

        return interArea / unionArea
    }

    companion object {
        private const val CONFIDENCE_THRESHOLD = 0.25F
        private const val IOU_THRESHOLD = 0.4F
        private const val INPUT_MEAN = 0f
        private const val INPUT_STD = 255f
        private val INPUT_TYPE = DataType.FLOAT32
        private val OUTPUT_TYPE = DataType.FLOAT32
    }
}