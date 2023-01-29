package fr.simon.marquis.preferencesmanager.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.ScrollButtonVisibility
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme

@Composable
fun ScrollBackUp(
    enabled: Boolean,
    onClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(
        if (enabled) ScrollButtonVisibility.Visible else ScrollButtonVisibility.Gone,
        label = "ScrollBackUp Transition"
    )

    val bottomOffset by transition.animateDp(label = "ScrollBackUp offset") {
        if (it == ScrollButtonVisibility.Gone) {
            (-24).dp
        } else {
            24.dp
        }
    }

    if (bottomOffset > 0.dp) {
        ExtendedFloatingActionButton(
            icon = {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    modifier = Modifier.height(18.dp),
                    contentDescription = null
                )
            },
            text = {
                Text(text = stringResource(id = R.string.scrollUp))
            },
            onClick = onClicked,
            modifier = modifier
                .offset(x = 0.dp, y = -bottomOffset)
                .height(36.dp)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun ScrollBackUpPreview() {
    AppTheme(isDarkTheme = isSystemInDarkTheme()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            ScrollBackUp(enabled = true, {})
        }
    }
}
