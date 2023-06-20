package fr.simon.marquis.preferencesmanager.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.PreferenceType
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme
import timber.log.Timber

@Composable
fun DialogPreference(
    openDialog: Boolean,
    preferenceType: PreferenceType,
    confirmButton: (key: String, value: Any?) -> Unit,
    deleteButton: (key: String) -> Unit,
    dismissButton: () -> Unit
) {
    if (!openDialog) {
        return
    }

    var newKey by remember(preferenceType) {
        mutableStateOf(preferenceType.key)
    }
    var newValue by remember(preferenceType) {
        val typeValue = when (preferenceType.value) {
            is Boolean -> preferenceType.value
            is Float,
            is Int,
            is Long,
            is String -> preferenceType.value as String

            is Set<*> -> {
                if ((preferenceType.value as Set<*>).all { it is String }) {
                    (preferenceType.value as Set<*>).joinToString(separator = ", ")
                } else {
                    throw IllegalArgumentException("Unknown StringSet type.")
                }
            }

            else -> throw IllegalArgumentException("Unknown Value type.")
        }
        mutableStateOf(typeValue)
    }
    val isValidPreference by remember(newKey, newValue) {
        val isValid = (newValue as? String)?.isNotEmpty() ?: (newValue as? Boolean)
            ?: throw IllegalArgumentException("This shouldn't throw, but a bad value was set") // :)
        mutableStateOf(isValid)
    }
    var confirmDelete by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        modifier = Modifier
            .padding(28.dp)
            .wrapContentHeight(),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = dismissButton,
        icon = {
            val icon = with(preferenceType) {
                if (isEdit) Icons.Default.Edit else Icons.Default.PlaylistAdd
            }
            Icon(imageVector = icon, contentDescription = null)
        },
        title = {
            val title = with(preferenceType) {
                if (isEdit) dialogTitleEdit else dialogTitleAdd
            }
            Text(text = stringResource(id = title))
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    label = { Text(text = stringResource(id = R.string.hint_key)) },
                    value = newKey,
                    onValueChange = { newKey = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (newValue is Boolean) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Value"
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = newValue as Boolean,
                            onCheckedChange = { newValue = it }
                        )
                    }
                } else {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 6,
                        label = { Text(text = stringResource(id = R.string.hint_value)) },
                        value = newValue as String,
                        onValueChange = { newValue = it }
                    )
                }
                if (confirmDelete) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Press delete again to delete value")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValidPreference,
                onClick = { confirmButton(newKey, newValue) }
            ) {
                val text = with(preferenceType) {
                    if (isEdit) R.string.dialog_update else R.string.dialog_add
                }
                Text(text = stringResource(id = text))
            }
        },
        dismissButton = {
            if (preferenceType.isEdit) {
                TextButton(
                    onClick = {
                        Timber.d("Clicked $confirmDelete")
                        if (!confirmDelete) {
                            confirmDelete = true
                            return@TextButton
                        }

                        deleteButton(newKey)
                    }
                ) {
                    Text(text = stringResource(id = R.string.dialog_delete))
                }
            }
            TextButton(onClick = dismissButton) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        }
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview_DialogPreferenceTextValue() {
    AppTheme(isDarkTheme = isSystemInDarkTheme()) {
        Surface(modifier = Modifier.fillMaxWidth()) {
            DialogPreference(
                openDialog = true,
                preferenceType = PreferenceType.fromObject("").apply {
                    key = "some text key"
                    value = "sajkhaffjlh"
                    isEdit = false
                },
                confirmButton = { _, _ -> },
                dismissButton = { },
                deleteButton = { }
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview_DialogPreferenceBoolean() {
    AppTheme(isDarkTheme = isSystemInDarkTheme()) {
        Surface(modifier = Modifier.fillMaxWidth()) {
            DialogPreference(
                openDialog = true,
                preferenceType = PreferenceType.fromObject(true).apply {
                    key = "some boolean key"
                    value = true
                    isEdit = true
                },
                confirmButton = { _, _ -> },
                dismissButton = { },
                deleteButton = { }
            )
        }
    }
}
