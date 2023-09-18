package fr.simon.marquis.preferencesmanager.ui.editor

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.EFontSize
import fr.simon.marquis.preferencesmanager.model.EFontTheme
import fr.simon.marquis.preferencesmanager.ui.components.AppBar
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun FileEditorMenu(
    onSave: () -> Unit,
    onFontTheme: (color: EFontTheme) -> Unit,
    onFontSize: (size: EFontSize) -> Unit
) {
    var isFontThemeShowing by remember { mutableStateOf(false) }
    var isFontSizeShowing by remember { mutableStateOf(false) }

    IconButton(onClick = onSave) {
        Icon(Icons.Default.Save, null)
    }
    IconButton(onClick = { isFontThemeShowing = !isFontThemeShowing }) {
        Icon(Icons.Default.Palette, null)
    }
    IconButton(onClick = { isFontSizeShowing = !isFontSizeShowing }) {
        Icon(Icons.Default.FormatSize, null)
    }

    DropdownMenu(
        expanded = isFontThemeShowing,
        onDismissRequest = { isFontThemeShowing = false }
    ) {
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.action_theme_eclipse))
            },
            leadingIcon = {
                Icon(Icons.Default.Brush, contentDescription = null)
            },
            onClick = {
                onFontTheme(EFontTheme.ECLIPSE)
                isFontThemeShowing = false
            }
        )
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.action_theme_google))
            },
            leadingIcon = {
                Icon(Icons.Default.Brush, contentDescription = null)
            },
            onClick = {
                onFontTheme(EFontTheme.GOOGLE)
                isFontThemeShowing = false
            }
        )
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.action_theme_roboticket))
            },
            leadingIcon = {
                Icon(Icons.Default.Brush, contentDescription = null)
            },
            onClick = {
                onFontTheme(EFontTheme.ROBOTICKET)
                isFontThemeShowing = false
            }
        )
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.action_theme_notepad))
            },
            leadingIcon = {
                Icon(Icons.Default.Brush, contentDescription = null)
            },
            onClick = {
                onFontTheme(EFontTheme.NOTEPAD)
                isFontThemeShowing = false
            }
        )
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.action_theme_netbeans))
            },
            leadingIcon = {
                Icon(Icons.Default.Brush, contentDescription = null)
            },
            onClick = {
                onFontTheme(EFontTheme.NETBEANS)
                isFontThemeShowing = false
            }
        )
    }

    DropdownMenu(
        expanded = isFontSizeShowing,
        onDismissRequest = { isFontSizeShowing = false }
    ) {
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.action_size_extra_small))
            },
            leadingIcon = {
                Icon(Icons.Default.FormatSize, contentDescription = null)
            },
            onClick = {
                onFontSize(EFontSize.EXTRA_SMALL)
                isFontSizeShowing = false
            }
        )
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.action_size_small))
            },
            leadingIcon = {
                Icon(Icons.Default.FormatSize, contentDescription = null)
            },
            onClick = {
                onFontSize(EFontSize.SMALL)
                isFontSizeShowing = false
            }
        )
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.action_size_medium))
            },
            leadingIcon = {
                Icon(Icons.Default.FormatSize, contentDescription = null)
            },
            onClick = {
                onFontSize(EFontSize.MEDIUM)
                isFontSizeShowing = false
            }
        )
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.action_size_large))
            },
            leadingIcon = {
                Icon(Icons.Default.FormatSize, contentDescription = null)
            },
            onClick = {
                onFontSize(EFontSize.LARGE)
                isFontSizeShowing = false
            }
        )
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.action_size_extra_large))
            },
            leadingIcon = {
                Icon(Icons.Default.FormatSize, contentDescription = null)
            },
            onClick = {
                onFontSize(EFontSize.EXTRA_LARGE)
                isFontSizeShowing = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview_FileEditorMenu() {
    AppTheme(isDarkTheme = isSystemInDarkTheme()) {
        AppBar(
            title = { Text(text = stringResource(id = R.string.app_name)) },
            actions = {
                FileEditorMenu(
                    onSave = {},
                    onFontTheme = {},
                    onFontSize = {}
                )
            },
            textState = MutableStateFlow(TextFieldValue(""))
        )
    }
}
