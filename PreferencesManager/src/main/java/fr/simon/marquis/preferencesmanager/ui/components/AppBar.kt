@file:OptIn(ExperimentalMaterial3Api::class)

package fr.simon.marquis.preferencesmanager.ui.components

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme

@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavIconPressed: () -> Unit = { },
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val backgroundColors = TopAppBarDefaults.centerAlignedTopAppBarColors()
    val minColor = backgroundColors.containerColor(colorTransitionFraction = 0f).value
    val maxColor = backgroundColors.containerColor(colorTransitionFraction = 1f).value
    val easing = FastOutLinearInEasing.transform(scrollBehavior?.state?.overlappedFraction ?: 0f)
    val backgroundColor = lerp(minColor, maxColor, easing)

    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(backgroundColor)

    val foregroundColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent
    )
    Box(modifier = Modifier.background(backgroundColor)) {
        SmallTopAppBar(
            modifier = modifier,
            actions = actions,
            title = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart,
                    content = { title() }
                )
            },
            scrollBehavior = scrollBehavior,
            colors = foregroundColors,
            navigationIcon = { /* TODO */ }
        )
    }
}

@Preview
@Composable
private fun Preview_AppBar(
    appName: String = stringResource(id = R.string.app_name)
) {
    AppTheme {
        AppBar(title = { Text(appName) })
    }
}

@Preview
@Composable
fun Preview_Dark_AppBar(
    appName: String = stringResource(id = R.string.app_name)
) {
    AppTheme(isDarkTheme = true) {
        AppBar(title = { Text(appName) })
    }
}
