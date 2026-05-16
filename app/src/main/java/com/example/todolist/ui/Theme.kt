package com.example.todolist.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// T-Day Concept Color Palette
// Warm gradient: 아침→점심→저녁 흐름

// Primary Colors - Warm Gradient
val TTodayYellow = Color(0xFFFFB84D)    // 밝고 따뜻한 황금색
val TTodayOrange = Color(0xFFFF9500)   // 활기차고 에너지 있는 주황색
val TTodayRed = Color(0xFFFF6B6B)      // 긴급/중요도 높음

// Status Colors
val TCompletedGreen = Color(0xFF51CF66)  // 완료 만족감
val TOverdueRed = Color(0xFFFF6B6B)      // 경고/지난 일정

// Priority Colors
val TPriorityHigh = Color(0xFFFF6B6B)    // 높음 - 빨강
val TPriorityNormal = Color(0xFFFFB84D)  // 보통 - 황금
val TPriorityLow = Color(0xFF51CF66)     // 낮음 - 초록

// Neutral Colors
val TTodoWhite = Color(0xFFFFFBF5)       // 따뜻한 화이트 (배경)
val TTodoSurface = Color(0xFFFFEFE6)     // 따뜻한 서피스
val TTodoOutline = Color(0xFFE8D4C4)     // 따뜻한 아웃라인

val TTodayColorScheme = lightColorScheme(
    primary = TTodayOrange,           // 주 컬러 - 따뜻한 주황
    onPrimary = Color.White,
    primaryContainer = TTodayYellow,  // 약한 primary
    onPrimaryContainer = Color(0xFF664400),
    
    secondary = TCompletedGreen,      // 완료 컬러
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4F5E0),
    onSecondaryContainer = Color(0xFF1B4D2E),
    
    tertiary = TTodayRed,             // 강조 - 경고/중요도
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDFDA),
    onTertiaryContainer = Color(0xFF660033),
    
    error = TTodayRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDFDA),
    onErrorContainer = Color(0xFF660033),
    
    background = TTodoWhite,
    onBackground = Color(0xFF3B2F2B),
    
    surface = TTodoSurface,
    onSurface = Color(0xFF3B2F2B),
    surfaceVariant = Color(0xFFE8D4C4),
    onSurfaceVariant = Color(0xFF664400),
    
    outline = TTodoOutline,
    outlineVariant = Color(0xFFD8C4B8)
)

@Composable
fun TTdoListTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TTodayColorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
