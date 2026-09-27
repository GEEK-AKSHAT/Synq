package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Synq Monogram S logo rendered matching the uploaded branding.
 * - In Dark theme: Outlined white rounded S.
 * - In Light theme: Solid black rounded S.
 */
@Composable
fun SynqLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    tint: Color = if (isSystemInDarkTheme()) Color.White else Color(0xFF111111),
    isDark: Boolean = isSystemInDarkTheme()
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val scaleX = w / 200f
        val scaleY = h / 200f

        val path = Path().apply {
            moveTo(140f * scaleX, 55f * scaleY)
            cubicTo(125f * scaleX, 35f * scaleY, 105f * scaleX, 35f * scaleY, 90f * scaleX, 38f * scaleY)
            cubicTo(65f * scaleX, 43f * scaleY, 55f * scaleX, 62f * scaleY, 55f * scaleX, 80f * scaleY)
            cubicTo(55f * scaleX, 108f * scaleY, 90f * scaleX, 115f * scaleY, 110f * scaleX, 122f * scaleY)
            cubicTo(135f * scaleX, 130f * scaleY, 148f * scaleX, 142f * scaleY, 148f * scaleX, 162f * scaleY)
            cubicTo(148f * scaleX, 185f * scaleY, 130f * scaleX, 205f * scaleY, 98f * scaleX, 205f * scaleY)
            cubicTo(70f * scaleX, 205f * scaleY, 50f * scaleX, 190f * scaleY, 42f * scaleX, 168f * scaleY)
            cubicTo(39f * scaleX, 158f * scaleY, 45f * scaleX, 148f * scaleY, 55f * scaleX, 148f * scaleY)
            cubicTo(63f * scaleX, 148f * scaleY, 68f * scaleX, 154f * scaleY, 72f * scaleX, 162f * scaleY)
            cubicTo(78f * scaleX, 175f * scaleY, 88f * scaleX, 182f * scaleY, 100f * scaleX, 182f * scaleY)
            cubicTo(115f * scaleX, 182f * scaleY, 125f * scaleX, 172f * scaleY, 125f * scaleX, 158f * scaleY)
            cubicTo(125f * scaleX, 138f * scaleY, 95f * scaleX, 130f * scaleY, 75f * scaleX, 122f * scaleY)
            cubicTo(52f * scaleX, 112f * scaleY, 35f * scaleX, 98f * scaleY, 35f * scaleX, 75f * scaleY)
            cubicTo(35f * scaleX, 48f * scaleY, 56f * scaleX, 20f * scaleY, 95f * scaleX, 16f * scaleY)
            cubicTo(120f * scaleX, 13f * scaleY, 145f * scaleX, 22f * scaleY, 158f * scaleX, 40f * scaleY)
            cubicTo(165f * scaleX, 50f * scaleY, 158f * scaleX, 60f * scaleY, 148f * scaleX, 60f * scaleY)
            cubicTo(144f * scaleX, 60f * scaleY, 142f * scaleX, 58f * scaleY, 140f * scaleX, 55f * scaleY)
            close()
        }

        if (isDark) {
            // Outlined stroke
            drawPath(
                path = path,
                color = tint,
                style = Stroke(width = 4f * scaleX, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        } else {
            // Solid filled
            drawPath(path = path, color = tint)
        }
    }
}

/**
 * Synq Wordmark matching the user's uploaded typography.
 */
@Composable
fun SynqWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 26.sp,
    color: Color = if (isSystemInDarkTheme()) Color.White else Color(0xFF111111)
) {
    Text(
        text = "synq",
        fontSize = fontSize,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.SansSerif,
        letterSpacing = (-0.5).sp,
        color = color,
        modifier = modifier
    )
}

/**
 * Combined Brand Header displaying both Monogram and Wordmark.
 */
@Composable
fun SynqBrandHeader(
    modifier: Modifier = Modifier,
    logoSize: Dp = 28.dp,
    fontSize: TextUnit = 24.sp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SynqLogoMark(size = logoSize)
        Spacer(modifier = Modifier.width(8.dp))
        SynqWordmark(fontSize = fontSize)
    }
}
