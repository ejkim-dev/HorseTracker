package com.example.horsetracker.presentation.ui.camera

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.example.horsetracker.presentation.detector.BoundingBox

class CameraViewModel: ViewModel() {
    private val _boundingBoxes = mutableStateOf<List<BoundingBox>>(emptyList())
    val boundingBoxes: State<List<BoundingBox>> = _boundingBoxes

    fun initCameraController(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        isFrontCamera: Boolean = false
    ) {

    }

    fun setFrameProcessor() {
    }

    fun onCameraInitialized() {
    }

    fun updateBoundingBoxes(newBoxes: List<BoundingBox>) {
        _boundingBoxes.value = newBoxes
    }
}