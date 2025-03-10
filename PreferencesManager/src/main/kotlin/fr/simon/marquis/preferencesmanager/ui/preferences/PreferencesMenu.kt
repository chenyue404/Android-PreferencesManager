package fr.simon.marquis.preferencesmanager.ui.preferences

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
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
import fr.simon.marquis.preferencesmanager.model.EPreferencesOverflow
import fr.simon.marquis.preferencesmanager.model.EPreferencesSort
import fr.simon.marquis.preferencesmanager.model.PreferenceType
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme
import fr.simon.marquis.preferencesmanager.util.PrefManager
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun PreferencesMenu(
    isFavorite: Boolean,
    onSearch: () -> Unit,
    onAddClicked: (value: PreferenceType) -> Unit,
    onOverflowClicked: (value: EPreferencesOverflow) -> Unit,
    onSortClicked: (value: EPreferencesSort) -> Unit
) {
    var isAddMenuShowing by remember { mutableStateOf(false) }
    var isOverflowMenuSowing by remember { mutableStateOf(false) }
    var isSortMenuShowing by remember { mutableStateOf(false) }

    IconButton(onClick = onSearch) {
        Icon(Icons.Default.Search, null)
    }
    IconButton(onClick = { isAddMenuShowing = !isAddMenuShowing }) {
        Icon(Icons.Default.Add, null)
    }
    IconButton(onClick = { isOverflowMenuSowing = !isOverflowMenuSowing }) {
        Icon(Icons.Default.MoreVert, null)
    }

    DropdownMenu(
        expanded = isAddMenuShowing,
        onDismissRequest = { isAddMenuShowing = false }
    ) {
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.action_add_int)) },
            onClick = {
                onAddClicked(PreferenceType.INT)
                isAddMenuShowing = false
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.action_add_string)) },
            onClick = {
                onAddClicked(PreferenceType.STRING)
                isAddMenuShowing = false
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.action_add_boolean)) },
            onClick = {
                onAddClicked(PreferenceType.BOOLEAN)
                isAddMenuShowing = false
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.action_add_float)) },
            onClick = {
                onAddClicked(PreferenceType.FLOAT)
                isAddMenuShowing = false
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.action_add_long)) },
            onClick = {
                onAddClicked(PreferenceType.LONG)
                isAddMenuShowing = false
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.action_add_stringset)) },
            onClick = {
                onAddClicked(PreferenceType.STRINGSET)
                isAddMenuShowing = false
            }
        )
    }

    DropdownMenu(
        expanded = isOverflowMenuSowing,
        onDismissRequest = { isOverflowMenuSowing = false }
    ) {
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.action_sort)) },
            leadingIcon = {
                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
            },
            trailingIcon = {
                Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = null)
            },
            onClick = {
                isSortMenuShowing = true
                isOverflowMenuSowing = false
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.action_direct_edit)) },
            leadingIcon = {
                Icon(Icons.Default.Edit, contentDescription = null)
            },
            onClick = {
                onOverflowClicked(EPreferencesOverflow.EDIT)
                isOverflowMenuSowing = false
            }
        )
        DropdownMenuItem(
            text = {
                if (isFavorite) {
                    Text(text = stringResource(id = R.string.action_unfav))
                } else {
                    Text(text = stringResource(id = R.string.action_fav))
                }
            },
            leadingIcon = {
                if (isFavorite) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null)
                } else {
                    Icon(Icons.Default.Favorite, contentDescription = null)
                }
            },
            onClick = {
                onOverflowClicked(EPreferencesOverflow.FAV)
                isOverflowMenuSowing = false
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.action_backup_file)) },
            leadingIcon = {
                Icon(Icons.Default.Download, contentDescription = null)
            },
            onClick = {
                onOverflowClicked(EPreferencesOverflow.BACKUP)
                isOverflowMenuSowing = false
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.action_restore_file)) },
            leadingIcon = {
                Icon(Icons.Default.Restore, contentDescription = null)
            },
            onClick = {
                onOverflowClicked(EPreferencesOverflow.RESTORE)
                isOverflowMenuSowing = false
            }
        )
    }

    DropdownMenu(
        expanded = isSortMenuShowing,
        onDismissRequest = { isSortMenuShowing = false }
    ) {
        DropdownMenuItem(
            enabled = PrefManager.keySortType == EPreferencesSort.TYPE_AND_ALPHANUMERIC.ordinal,
            text = { Text(text = stringResource(id = R.string.action_sort_alpha)) },
            leadingIcon = {
                Icon(Icons.Default.SortByAlpha, contentDescription = null)
            },
            onClick = {
                onSortClicked(EPreferencesSort.ALPHANUMERIC)
                isSortMenuShowing = false
            }
        )
        DropdownMenuItem(
            enabled = PrefManager.keySortType == EPreferencesSort.ALPHANUMERIC.ordinal,
            text = { Text(text = stringResource(id = R.string.action_sort_type)) },
            leadingIcon = {
                Icon(Icons.Default.Category, contentDescription = null)
            },
            onClick = {
                onSortClicked(EPreferencesSort.TYPE_AND_ALPHANUMERIC)
                isSortMenuShowing = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview_PreferencesMenu() {
    AppTheme(isDarkTheme = isSystemInDarkTheme()) {
        PreferencesAppBar(
            scrollBehavior = null,
            searchText = MutableStateFlow(TextFieldValue("")),
            state = PreferencesState(
                pkgIcon = null,
                pkgName = "com.some.cool.app",
                pkgTitle = "Some Cool App"
            ),
            onAddClicked = {},
            onBackPressed = {},
            onOverflowClicked = {},
            onIsSearching = {},
            onIsNotSearching = {},
            onSortClicked = {}
        )
    }
}
