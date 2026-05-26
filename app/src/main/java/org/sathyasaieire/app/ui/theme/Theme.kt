package org.sathyasaieire.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = SaffronOnPrimary,
    primaryContainer = SaffronPrimaryContainer,
    onPrimaryContainer = SaffronOnPrimaryContainer,
    secondary = SaffronSecondary,
    onSecondary = SaffronOnSecondary,
    secondaryContainer = SaffronSecondaryContainer,
    onSecondaryContainer = SaffronOnSecondaryContainer,
    tertiary = SaffronTertiary,
    onTertiary = SaffronOnTertiary,
    tertiaryContainer = SaffronTertiaryContainer,
    onTertiaryContainer = SaffronOnTertiaryContainer,
    background = SaffronBackground,
    onBackground = SaffronOnBackground,
    surface = SaffronSurface,
    onSurface = SaffronOnSurface,
    surfaceVariant = SaffronSurfaceVariant,
    onSurfaceVariant = SaffronOnSurfaceVariant,
    outline = SaffronOutline,
    outlineVariant = SaffronOutlineVariant,
    error = SaffronError,
    onError = SaffronOnError,
    errorContainer = SaffronErrorContainer,
    onErrorContainer = SaffronOnErrorContainer,
    inverseSurface = SaffronInverseSurface,
    inverseOnSurface = SaffronInverseOnSurface,
    inversePrimary = SaffronInversePrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary = SaffronDarkPrimary,
    onPrimary = SaffronDarkOnPrimary,
    primaryContainer = SaffronDarkPrimaryContainer,
    onPrimaryContainer = SaffronDarkOnPrimaryContainer,
    secondary = SaffronDarkSecondary,
    onSecondary = SaffronDarkOnSecondary,
    secondaryContainer = SaffronDarkSecondaryContainer,
    onSecondaryContainer = SaffronDarkOnSecondaryContainer,
    tertiary = SaffronDarkTertiary,
    onTertiary = SaffronDarkOnTertiary,
    tertiaryContainer = SaffronDarkTertiaryContainer,
    onTertiaryContainer = SaffronDarkOnTertiaryContainer,
    background = SaffronDarkBackground,
    onBackground = SaffronDarkOnBackground,
    surface = SaffronDarkSurface,
    onSurface = SaffronDarkOnSurface,
    surfaceVariant = SaffronDarkSurfaceVariant,
    onSurfaceVariant = SaffronDarkOnSurfaceVariant,
    outline = SaffronDarkOutline,
    outlineVariant = SaffronDarkOutlineVariant,
    error = SaffronDarkError,
    onError = SaffronDarkOnError,
    errorContainer = SaffronDarkErrorContainer,
    onErrorContainer = SaffronDarkOnErrorContainer,
    inverseSurface = SaffronDarkInverseSurface,
    inverseOnSurface = SaffronDarkInverseOnSurface,
    inversePrimary = SaffronDarkInversePrimary,
)

@Composable
fun SaiIrelandTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = SaiTypography,
        content = content,
    )
}
