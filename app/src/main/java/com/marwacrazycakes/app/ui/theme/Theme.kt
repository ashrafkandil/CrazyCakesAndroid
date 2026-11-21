package com.marwacrazycakes.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.marwacrazycakes.app.data.ThemeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MarwaCrazyCakesTheme(content: @Composable () -> Unit) {
    var colorScheme by remember { mutableStateOf<androidx.compose.material3.ColorScheme?>(null) }
    var initialized by remember { mutableStateOf(false) }

    if (!initialized) {
        initialized = true
        CoroutineScope(Dispatchers.IO).launch {
            val theme = ThemeRepository.getDefaultTheme()
            if (theme != null) {
                androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot { snapshot ->
                    colorScheme = theme.toColorScheme()
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme ?: androidx.compose.material3.lightColorScheme(
            primary = com.marwacrazycakes.app.ui.theme.Primary,
            onPrimary = com.marwacrazycakes.app.ui.theme.PrimaryForeground,
            secondary = com.marwacrazycakes.app.ui.theme.Secondary,
            onSecondary = com.marwacrazycakes.app.ui.theme.SecondaryForeground,
            background = com.marwacrazycakes.app.ui.theme.Background,
            onBackground = com.marwacrazycakes.app.ui.theme.Foreground,
            surface = com.marwacrazycakes.app.ui.theme.Card,
            onSurface = com.marwacrazycakes.app.ui.theme.Foreground,
            surfaceVariant = com.marwacrazycakes.app.ui.theme.Muted,
            onSurfaceVariant = com.marwacrazycakes.app.ui.theme.MutedForeground,
            outline = com.marwacrazycakes.app.ui.theme.Border,
            outlineVariant = com.marwacrazycakes.app.ui.theme.Border,
        ),
        typography = AppTypography,
        shapes = Shapes(),
        content = content,
    )
}
