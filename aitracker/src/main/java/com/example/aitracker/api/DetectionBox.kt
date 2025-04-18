package com.example.aitracker.api

/**
 * 객체 감지 결과를 나타내는 공개 데이터 클래스
 */
data class DetectionBox(
    val left: Float,      // 왼쪽 상단 x 좌표 (0-1)
    val top: Float,       // 왼쪽 상단 y 좌표 (0-1)
    val right: Float,     // 오른쪽 하단 x 좌표 (0-1)
    val bottom: Float,    // 오른쪽 하단 y 좌표 (0-1)
    val label: String,    // 객체 라벨
    val confidence: Float // 신뢰도 (0-1)
)