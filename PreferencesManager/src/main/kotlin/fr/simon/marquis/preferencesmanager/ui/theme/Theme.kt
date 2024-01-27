package fr.simon.marquis.preferencesmanager.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import fr.simon.marquis.preferencesmanager.util.PrefManager

private val darkColors = darkColorScheme()

private val lightColors = lightColorScheme()

@Composable
fun AppTheme(
    isDarkTheme: Boolean,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    /* If were looking at edit previews, fool it and init preferences to render */
    val view = LocalView.current
    if (view.isInEditMode) {
        val context = LocalContext.current
        PrefManager.init(context)
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        isDarkTheme -> darkColors
        else -> lightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
