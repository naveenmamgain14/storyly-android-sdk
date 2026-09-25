package com.storyly.sdk.internal.ui

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Self-contained styling. The SDK draws its own look rather than reading a
 * MaterialTheme, so it renders identically in any host app.
 */
internal object StorylyTokens {

    val unseenRing: Brush = Brush.linearGradient(
        listOf(Color(0xFFFB5C5C), Color(0xFFD93F77), Color(0xFFC135C6))
    )
    val seenRing: Brush = Brush.linearGradient(listOf(Color(0xFFD4D4D8), Color(0xFFD4D4D8)))

    val surface: Color = Color.White
    val placeholder: Color = Color(0xFFE4E4E7)
    val mutedText: Color = Color(0xFF6B7280)
    val accent: Color = Color(0xFF6750A4)
    val scrim: Color = Color.Black

    val circleSize = 70.dp
    val ringWidth = 3.dp
    val itemWidth = 80.dp

    val label: TextStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF18181B))
    val message: TextStyle = TextStyle(fontSize = 13.sp, color = mutedText)
    val action: TextStyle = TextStyle(fontSize = 14.sp, color = accent, fontWeight = FontWeight.Medium)
    val viewerTitle: TextStyle = TextStyle(fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
    val viewerCounter: TextStyle = TextStyle(fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
    val avatarInitial: TextStyle = TextStyle(fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
}
