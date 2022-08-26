@file:OptIn(ExperimentalMaterial3Api::class)

package fr.simon.marquis.preferencesmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme
import fr.simon.marquis.preferencesmanager.util.PrefManager
import kotlinx.coroutines.flow.MutableStateFlow

private val slideIn = {
    slideIn(
        initialOffset = { IntOffset(it.width, 0) },
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold
        )

    )
}
private val slideUp = {
    slideOut(
        targetOffset = { IntOffset(0, -it.height) },
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold
        )
    )
}

@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavIconPressed: () -> Unit = { },
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    textState: MutableStateFlow<TextFieldValue>,
    isSearching: Boolean = false,
    onSearchClose: () -> Unit = {}
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

        AnimatedVisibility(
            visible = isSearching,
            enter = slideIn(),
            exit = slideUp(),
        ) {
            SearchView(
                backgroundColor = backgroundColor,
                state = textState,
                onClose = onSearchClose
            )
        }
    }
}

@Composable
fun SearchView(
    backgroundColor: Color,
    state: MutableStateFlow<TextFieldValue>,
    onClose: () -> Unit
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(64.dp),
        label = { Text("Search apps...") },
        textStyle = TextStyle(fontSize = 18.sp),
        singleLine = true,
        shape = RectangleShape, // The TextFiled has rounded corners top left and right by default
        value = state.collectAsState().value,
        onValueChange = { value ->
            state.value = value
        },
        leadingIcon = {
            IconButton(
                onClick = {
                    onClose()
                    state.value = TextFieldValue("")
                }
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null,
                )
            }
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    // Remove text from TextField when you press the 'X' icon
                    state.value = TextFieldValue("")
                }
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null
                )
            }
        },
        colors = TextFieldDefaults.textFieldColors(
            containerColor = backgroundColor
        )
    )
}

@Preview
@Composable
private fun Preview_AppBar(
    appName: String = stringResource(id = R.string.app_name)
) {
    val textState = MutableStateFlow(TextFieldValue(""))
    val context = LocalContext.current
    PrefManager.init(context)

    AppTheme {
        AppBar(title = { Text(appName) }, textState = textState)
    }
}

@Preview
@Composable
fun Preview_Dark_AppBar(
    appName: String = stringResource(id = R.string.app_name)
) {
    val textState = MutableStateFlow(TextFieldValue(""))
    val context = LocalContext.current
    PrefManager.init(context)

    AppTheme(isDarkTheme = true) {
        AppBar(title = { Text(appName) }, textState = textState)
    }
}
