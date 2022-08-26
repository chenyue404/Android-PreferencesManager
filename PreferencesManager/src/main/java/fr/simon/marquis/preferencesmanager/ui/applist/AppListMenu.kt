package fr.simon.marquis.preferencesmanager.ui.applist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.ui.components.AppBar
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme
import fr.simon.marquis.preferencesmanager.util.Utils

@Composable
fun AppListMenu(
    isMenuShowing: Boolean,
    setMenuShowing: (value: Boolean) -> Unit,
    onSearch: () -> Unit,
    onShowSystemApps: () -> Unit,
    onSwitchTheme: () -> Unit,
    onAbout: () -> Unit,
) {
    IconButton(onClick = onSearch) {
        Icon(Icons.Default.Search, null)
    }
    IconButton(onClick = { setMenuShowing(!isMenuShowing) }) {
        Icon(Icons.Default.MoreVert, null)
    }
    DropdownMenu(
        expanded = isMenuShowing,
        onDismissRequest = { setMenuShowing(false) }
    ) {
        val context = LocalContext.current
        val menuText = if (Utils.isShowSystemApps(context)) {
            R.string.hide_system_apps
        } else {
            R.string.show_system_apps
        }
        DropdownMenuItem(
            text = { Text(text = stringResource(id = menuText)) },
            onClick = {
                onShowSystemApps()
                setMenuShowing(false)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.switch_theme)) },
            onClick = {
                onSwitchTheme()
                setMenuShowing(false)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.show_popup)) },
            onClick = {
                onAbout()
                setMenuShowing(false)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun Preview_AppListMenu(
    appName: String = stringResource(id = R.string.app_name)
) {
    var isMenuShowing by remember { mutableStateOf(false) }

    AppTheme {
        AppBar(
            title = { Text(text = appName) },
            actions = {
                AppListMenu(
                    isMenuShowing = isMenuShowing,
                    setMenuShowing = { value -> isMenuShowing = value },
                    onSearch = {},
                    onShowSystemApps = {},
                    onSwitchTheme = {},
                    onAbout = {}
                )
            }
        )
    }
}
