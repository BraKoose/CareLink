package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = CoolTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface
  )

private val LightColorScheme =
  lightColorScheme(
    primary = TealPrimary,
    secondary = SageSecondary,
    tertiary = CoolTertiary,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface
  )

private val PinkColorScheme =
  lightColorScheme(
    primary = PinkPrimary,
    secondary = PinkSecondary,
    tertiary = CoolTertiary,
    background = PinkBackground,
    surface = PinkSurface,
    onBackground = PinkOnBackground,
    onSurface = PinkOnBackground
  )

private val EmeraldColorScheme =
  lightColorScheme(
    primary = EmeraldPrimary,
    secondary = EmeraldSecondary,
    tertiary = CoolTertiary,
    background = EmeraldBackground,
    surface = EmeraldSurface,
    onBackground = EmeraldOnBackground,
    onSurface = EmeraldOnBackground
  )

private val SunsetColorScheme =
  lightColorScheme(
    primary = SunsetPrimary,
    secondary = SunsetSecondary,
    tertiary = CoolTertiary,
    background = SunsetBackground,
    surface = SunsetSurface,
    onBackground = SunsetOnBackground,
    onSurface = SunsetOnBackground
  )

@Composable
fun MyApplicationTheme(
  themeType: String = "CLASSIC",
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when (themeType) {
      "PINK_ORCHID" -> PinkColorScheme
      "EMERALD" -> EmeraldColorScheme
      "WARM_SUNSET" -> SunsetColorScheme
      "DARK_SLATE" -> DarkColorScheme
      else -> {
        if (darkTheme) DarkColorScheme else LightColorScheme
      }
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
