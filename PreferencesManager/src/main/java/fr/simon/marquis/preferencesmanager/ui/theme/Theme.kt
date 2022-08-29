package fr.simon.marquis.preferencesmanager.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import fr.simon.marquis.preferencesmanager.util.PrefManager

private val darkColors = darkColorScheme()

private val lightColors = lightColorScheme()

@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (view.isInEditMode) {
        val context = LocalContext.current
        PrefManager.init(context)
    }

    val colorScheme = when (PrefManager.themePreference) {
        0 -> lightColors
        1 -> darkColors
        else -> {
            when {
                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = LocalContext.current
                    if (isDarkTheme)
                        dynamicDarkColorScheme(context)
                    else
                        dynamicLightColorScheme(context)
                }
                isDarkTheme -> darkColors
                else -> lightColors
            }
        }
    }

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    DisposableEffect(systemUiController, useDarkIcons) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )

        onDispose {}
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
