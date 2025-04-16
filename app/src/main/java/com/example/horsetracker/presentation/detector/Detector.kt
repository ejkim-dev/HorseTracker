package com.example.horsetracker.presentation.detector

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

class Detector(
    private val context: Context,
    private val modelPath: String,
    private val labelList: List<String>,
    private val detectorListener: DetectorListener
) {
    private var interpreter: Interpreter? = null

    private var tensorWidth = 0
    private var tensorHeight = 0
    private var numChannel = 0
    private var numElements = 0

    private val imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(INPUT_MEAN, INPUT_STANDARD_DEVIATION))
        .add(CastOp(INPUT_IMAGE_TYPE))
        .build()

    fun setup() {
        val model = FileUtil.loadMappedFile(context, modelPath)

        val options = Interpreter.Options().apply {
            numThreads = 4
        }
        interpreter = Interpreter(model, options)

        // 모델의 입력/출력 텐서 shape 가져오기
        val inputShape = interpreter?.getInputTensor(0)?.shape() ?: return
        val outputShape = interpreter?.getOutputTensor(0)?.shape() ?: return

        // inputShape = [1, 320, 320, 3], outputShape = [1, 5, 2100]
        tensorWidth = inputShape[1] // 320
        tensorHeight = inputShape[2] // 320
        numChannel = outputShape[1] // 5
        numElements = outputShape[2] // 2100
    }

    fun detect(image: Bitmap) {
        interpreter ?: run {
            detectorListener.onDetectError(NullPointerException("Interpreter is null"))
            return
        }
        if (tensorWidth == 0 || tensorHeight == 0 || numChannel == 0 || numElements == 0) {
            detectorListener.onDetectError(NullPointerException("Tensor shape is zero"))
            return
        }

        // Bitmap을 모델 입력 크기(320x320)로 리사이즈
        val resizedBitmap = Bitmap.createScaledBitmap(image, tensorWidth, tensorHeight, false)

        // TensorFlow Lite용으로 전처리 (정규화: 0~255 -> 0~1)
        val tensorImage = TensorImage(OUTPUT_IMAGE_TYPE)
        tensorImage.load(resizedBitmap)
        val processedImage = imageProcessor.process(tensorImage)
        val imageBuffer = processedImage.buffer

        // 추론 결과를 담을 output 버퍼 준비 [1, numChannel, numElements]
        val output = TensorBuffer.createFixedSize(
                intArrayOf(1, numChannel, numElements), // [1,5,2100]
                OUTPUT_IMAGE_TYPE
        )

        // TFLite 추론: input → output
        interpreter?.run(imageBuffer, output.buffer)

        // 결과 파싱해서 바운딩박스(말)를 추출
        val bestBoxes = bestBox(output.floatArray)

        if (bestBoxes == null) {
            detectorListener.onEmptyDetect()
            return
        }

        detectorListener.onDetect(bestBoxes)
    }

    private fun bestBox(array: FloatArray): List<BoundingBox>? {
        // array.length = 5 * 2100 = 10500 (배치=1을 제외)
        val boundingBoxes = mutableListOf<BoundingBox>()

        for (c in 0 until numElements) {
            // c : 2100개의 anchor/box 인덱스
            var maxConf = -1.0f
            var maxIdx = -1
            var j = 4
            var arrayIdx = c + numElements * j
            // j=4는 보통 confidence 채널
            while (j < numChannel) {
                if (array[arrayIdx] > maxConf) {
                    maxConf = array[arrayIdx]
                    maxIdx = j - 4
                }
                j++
                arrayIdx += numElements
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                val clsName = labelList[maxIdx]
                val cx = array[c] // 0
                val cy = array[c + numElements] // 1
                val w = array[c + numElements * 2]
                val h = array[c + numElements * 3]

                // (x1,y1)~(x2,y2) = 좌측 상단, 우측 하단
                val x1 = cx - (w / 2F)
                val y1 = cy - (h / 2F)
                val x2 = cx + (w / 2F)
                val y2 = cy + (h / 2F)

                // 이 때 x1,y1,x2,y2가 [0..1] 범위여야 화면 안에 있다
                if ((x1 < 0F || x1 > 1F) || (x2 < 0F || x2 > 1F) || (y1 < 0F || y1 > 1F) || (y2 < 0F || y2 > 1F))
                    continue

                boundingBoxes.add(
                    BoundingBox(
                        x1 = x1, y1 = y1, x2 = x2, y2 = y2,
                        cx = cx, cy = cy, w = w, h = h,
                        cnf = maxConf, cls = maxIdx, clsName = clsName
                    )
                )
            }
        }

        if (boundingBoxes.isEmpty()) return null

        // 박스들 중복제거(NMS) 후 반환
        return applyNMS(boundingBoxes)
    }
    /** NMS(Non-Max Suppression) 로직 : 객체 감지 모델에서 마지막 단계로 사용되며, 한 객체 주변에 여러 박스가 생기는 현상을 정리해 주는 역할
     *
     * 서로 겹치는 박스가 여러 개 있을 때, Confidence가 가장 높은 박스를 우선 선택하고,
     * 그 박스와 IoU(겹치는 영역 비율)가 높은 박스들은 제거해 가면서 중복을 줄임
     *
     * 	1. sortedByDescending { it.cnf }
     * 	    - 박스 리스트(boxes)를 confidence(cnf)가 높은 순서대로 정렬
     * 	    - 즉, 가장 확실도가 높은 박스부터 차례대로 처리를 하기 위함
     * 	2. 반복(while (sortedBoxes.isNotEmpty()))
     * 	    - 정렬된 박스들이 남아 있는 동안 계속 NMS를 수행
     * 	3. 가장 높은 박스(first)를 꺼내서 selectedBoxes에 넣고, sortedBoxes에서는 제거
     * 	    - 이 박스가 이 배치에서 확신도가 가장 높은 박스
     * 	4. 나머지 박스들과의 IoU(겹침 정도)를 계산 (calculateIoU(first, nextBox)).
     * 	    - IoU >= IOU_THRESHOLD(예: 0.4, 0.5 등)이면, “두 박스가 많이 겹친다”고 판단하여 iterator.remove()로 제거
     * 	    - 이미 확신도가 높은 박스(first)와 크게 겹치면, 굳이 또 다른 박스로 인식할 필요가 없다고 봄
     * 	5.	모든 박스를 소진할 때까지(더 이상 박스가 없을 때까지) 이 과정을 반복
     * 	6.	최종적으로 selectedBoxes에 NMS가 끝난 뒤의 박스들이 담김
     * 	*/
    private fun applyNMS(boxes: List<BoundingBox>): MutableList<BoundingBox> {
        val sortedBoxes = boxes.sortedByDescending { it.cnf }.toMutableList()
        val selectedBoxes = mutableListOf<BoundingBox>()

        while (sortedBoxes.isNotEmpty()) {
            val first = sortedBoxes.first()
            selectedBoxes.add(first)
            sortedBoxes.remove(first)

            val iterator = sortedBoxes.iterator()
            while (iterator.hasNext()) {
                val nextBox = iterator.next()
                val iou = calculateIoU(first, nextBox)
                if (iou >= IOU_THRESHOLD) {
                    iterator.remove()
                }
            }
        }

        return selectedBoxes
    }

    /** calculateIoU 함수는 두 박스가 어느 정도 겹치는지를 0~1 범위로 계산하는 표준 식을 구현한 것
     *
     * 0이면 “완전히 떨어져 있음”, 1이면 “둘이 완전 동일
     * */
    private fun calculateIoU(box1: BoundingBox, box2: BoundingBox): Float {
        val x1 = maxOf(box1.x1, box2.x1)
        val y1 = maxOf(box1.y1, box2.y1)
        val x2 = minOf(box1.x2, box2.x2)
        val y2 = minOf(box1.y2, box2.y2)

        val intersectionArea = maxOf(0F, x2 - x1) * maxOf(0F, y2 - y1)

        val box1Area = box1.w * box1.h
        val box2Area = box2.w * box2.h

        return intersectionArea / (box1Area + box2Area - intersectionArea)
    }

    companion object {
        private const val INPUT_MEAN = 0f
        private const val INPUT_STANDARD_DEVIATION = 255f
        private const val CONFIDENCE_THRESHOLD = 0.25F
        private const val IOU_THRESHOLD = 0.4F
        private val INPUT_IMAGE_TYPE = DataType.FLOAT32
        private val OUTPUT_IMAGE_TYPE = DataType.FLOAT32
    }


    interface DetectorListener {
        fun onDetect(boundingBoxes: List<BoundingBox>)
        fun onEmptyDetect()
        fun onDetectError(e: Exception)
    }
}