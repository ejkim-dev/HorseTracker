# HorseTracker App

## 프로젝트 개요
이 앱은 카메라와 AI 기반 객체 탐지 모델을 사용해 영상 속 말의 위치를 인식하고 바운딩 박스로 표시하는 앱 입니다.
특히, 2마리의 말이 등장하는 영상에서 한 마리만 추적하여 끝까지 따라가는 기능을 구현한 것이 핵심입니다.


## 핵심 기능
- 실시간 말 탐지 및 추적

## 기술 스택
- 언어: Kotlin 2.0.0
- UI 프레임워크: Jetpack Compose 2024.04.01
- 아키텍처: Clean Architecture
- 카메라 API: CameraX 1.4.2
- AI 모델: Ultralytics의 YOLOv11 모델([Ultralytics 객체 탐지 모델 공식 문서](https://docs.ultralytics.com/ko/tasks/detect/))
- 빌드 도구: Gradle 8.8.2

## 프로젝트 구조
```
com.example.horsetracker
├── di                          # 의존성 주입
├── domain                      # 도메인 레이어
│   ├── model                   # 도메인 모델
│   ├── repository              # 리포지토리 인터페이스
│   └── usecase                 # 유즈케이스
├── data                        # 데이터 레이어
│   ├── mapper                  # 데이터 매퍼
│   └── repository              # 리포지토리 구현체
└── presentation                # 프레젠테이션 레이어
    ├── camera                  # 카메라 관련 Feature
    ├── model                   # UI 모델
    ├── component               # UI 컴포넌트
    └── theme                   # UI 테마

com.example.aitracker        # TensorFlow Lite를 기반으로 한 객체 탐지 기능을 제공하는 라이브러리 모듈입니다. 
├── api                      # 공개 API 인터페이스
│   ├── DetectionBox.kt      # 탐지된 객체 정보를 담는 모델 클래스
│   ├── Detector.kt          # 객체 탐지 인터페이스
│   └── DetectorFactory.kt   # 탐지기 생성 팩토리 클래스
└── core                     # 내부 구현 클래스
    ├── BoundingBox.kt       # 내부 경계 상자 표현 클래스
    └── TFLiteDetector.kt    # TensorFlow Lite 기반 탐지기 구현
```

## Demo 영상
<img src="https://github.com/ejkim-dev/HorseTracker/blob/main/demo/demo.gif" width="25%">
