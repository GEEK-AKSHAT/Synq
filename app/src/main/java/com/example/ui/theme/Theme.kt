package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SynqPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = SynqPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = SynqSecondary,
    onSecondary = Color.White,
    tertiary = SynqTertiary,
    background = SynqDarkBackground,
    onBackground = SynqDarkOnSurface,
    surface = SynqDarkSurface,
    onSurface = SynqDarkOnSurface,
    surfaceVariant = SynqDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFA19EBB)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SynqPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9E4FF),
    onPrimaryContainer = SynqPrimaryDark,
    secondary = SynqSecondary,
    onSecondary = Color.White,
    tertiary = Color(0xFF0091A8),
    background = SynqLightBackground,
    onBackground = SynqLightOnSurface,
    surface = SynqLightSurface,
    onSurface = SynqLightOnSurface,
    surfaceVariant = SynqLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF5A586B)
  )

@Composable
fun SynqTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) = SynqTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
