package com.example.horsetracker.presentation.ui.camera

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

/**
 * CameraFactory: CameraX 관련 UseCase나 CameraProvider를 생성/제공
 */
object CameraFactory {

    /**
     * CameraProvider를 onReady 콜백에 전달
     */
    fun getCameraProvider(
        context: Context,
        onReady: (ProcessCameraProvider) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        val providerFuture = ProcessCameraProvider.getInstance(context)

        // addListener로 콜백 등록
        providerFuture.addListener({
            try {
                val cameraProvider = providerFuture.get()
                onReady(cameraProvider)
            } catch (e: Exception) {
                onError(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Preview UseCase 생성
     */
    fun createPreviewUseCase(): Preview {
        return Preview.Builder().build()
    }

    /**
     * ImageCapture UseCase 생성
     */
    fun createImageCaptureUseCase(): ImageCapture {
        return ImageCapture.Builder().build()
    }

    /**
     * Camera를 실제로 바인딩하는 함수
     * @param cameraProvider: 카메라 프로바이더
     * @param lifecycleOwner: UI 쪽에서 넘겨주는 LifecycleOwner
     * @param lensFacing: 전/후면
     * @param previewUseCase / imageCaptureUseCase: 미리 만들어둔 UseCase들
     */
    fun bindCameraUseCases(
        cameraProvider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        lensFacing: Int,
        previewUseCase: Preview,
        imageCaptureUseCase: ImageCapture
    ): Camera {
        // 먼저 unbindAll
        cameraProvider.unbindAll()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        return cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            previewUseCase,
            imageCaptureUseCase
        )
    }
}