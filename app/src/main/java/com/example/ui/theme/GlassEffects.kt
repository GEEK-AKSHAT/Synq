package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GlassEffects {

    // Instagram style story ring gradient (vibrant orange-magenta-purple-blue)
    val InstaStoryGradient = Brush.sweepGradient(
        colors = listOf(
            Color(0xFFF58529),
            Color(0xFFFEDA77),
            Color(0xFFDD2A7B),
            Color(0xFF8134AF),
            Color(0xFF515BD4),
            Color(0xFFF58529)
        )
    )

    // Liquid neon gradient
    val LiquidNeonGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6C5CE7),
            Color(0xFFFD79A8),
            Color(0xFF00CEC9)
        )
    )

    // Seen story border
    val SeenStoryBorder = Brush.linearGradient(
        colors = listOf(
            Color(0xFF888888),
            Color(0xFF666666)
        )
    )
}

/**
 * Applies a translucent liquid frosted glass surface with border highlights.
 */
@Composable
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
    borderColor: Color = Color.White.copy(alpha = 0.18f),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 8.dp
): Modifier {
    val highlightBrush = Brush.linearGradient(
        colors = listOf(
            borderColor,
            borderColor.copy(alpha = 0.04f)
        )
    )
    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.25f),
            spotColor = Color(0xFF6C5CE7).copy(alpha = 0.2f)
        )
        .clip(shape)
        .background(backgroundColor)
        .border(
            border = BorderStroke(borderWidth, highlightBrush),
            shape = shape
        )
}
