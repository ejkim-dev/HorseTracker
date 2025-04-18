package com.example.horsetracker.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.horsetracker.data.repository.DetectorRepositoryImpl
import com.example.horsetracker.domain.repository.DetectorRepository
import com.example.horsetracker.domain.usecase.DetectObjectsUseCase
import com.example.horsetracker.presentation.camera.CameraViewModel

object ServiceLocator {
    private var detectorRepository: DetectorRepository? = null
    private var detectObjectsUseCase: DetectObjectsUseCase? = null

    private fun provideDetectorRepository(context: Context): DetectorRepository {
        return detectorRepository ?: synchronized(this) {
            DetectorRepositoryImpl(context.applicationContext).also {
                detectorRepository = it
            }
        }
    }

    fun provideDetectObjectsUseCase(context: Context): DetectObjectsUseCase {
        return detectObjectsUseCase ?: synchronized(this) {
            DetectObjectsUseCase(provideDetectorRepository(context)).also {
                detectObjectsUseCase = it
            }
        }
    }

    fun provideCameraViewModelFactory(context: Context): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CameraViewModel(provideDetectObjectsUseCase(context)) as T
            }
        }
    }
}