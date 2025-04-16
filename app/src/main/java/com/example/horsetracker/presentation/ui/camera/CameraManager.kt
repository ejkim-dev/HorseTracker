package com.example.horsetracker.presentation.ui.camera

import android.content.Context
import android.net.Uri
import androidx.camera.core.Camera
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var imageCaptureUseCase: ImageCapture? = null
    private var camera: Camera? = null

    /**
     * 카메라 초기화 (콜백 방식)
     *
     * @param lensFacing 전면/후면 지정
     * @param onInitialized 카메라 준비 완료 시점
     * @param onError 에러 발생 시점
     */
    fun initializeCamera(
        lensFacing: Int,
        onInitialized: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        // 1) CameraProvider 가져오기
        CameraFactory.getCameraProvider(
            context = context,
            onReady = { provider ->
                cameraProvider = provider

                // 2) UseCase 생성
                previewUseCase = CameraFactory.createPreviewUseCase()
                imageCaptureUseCase = CameraFactory.createImageCaptureUseCase()

                // 3) bind
                camera = CameraFactory.bindCameraUseCases(
                    cameraProvider = provider,
                    lifecycleOwner = lifecycleOwner,
                    lensFacing = lensFacing,
                    previewUseCase = previewUseCase!!,
                    imageCaptureUseCase = imageCaptureUseCase!!
                )

                // 완료 콜백
                onInitialized()
            },
            onError = { e ->
                onError(e)
            }
        )
    }

    /**
     * 사진 촬영 (CameraX ImageCapture)
     * @param fileName: 저장할 파일 이름
     * @param onImageSaved: 촬영/저장 성공 시 호출, Uri를 반환
     * @param onError: 촬영 중 에러
     */
    fun takePicture(
        fileName: String? = null,
        onImageSaved: (uri: Uri) -> Unit = {},
        onError: (exception: ImageCaptureException) -> Unit = {}
    ) {
        val imageCapture = imageCaptureUseCase ?: run {
            onError(ImageCaptureException(ImageCapture.ERROR_INVALID_CAMERA, "ImageCapture is null", null))
            return
        }
        val defaultFileName = "image_" + System.currentTimeMillis() + ".jpg"

        //  파일 경로: 외부 캐시
        val outputFile = File(context.externalCacheDir, fileName ?: defaultFileName)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        val callback = object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // Q 미만 단말에서는 savedUri가 null 가능성 있음
                val savedUri = outputFileResults.savedUri ?: Uri.fromFile(outputFile)
                onImageSaved(savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }

        // 촬영 실행
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            callback
        )
    }

    /**
     * Compose 쪽에서 PreviewView.surfaceProvider를 연결
     */
    fun setPreviewSurface(surfaceProvider: Preview.SurfaceProvider?) {
        previewUseCase?.surfaceProvider = surfaceProvider
    }
}