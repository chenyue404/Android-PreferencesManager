package fr.simon.marquis.preferencesmanager.ui.components

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import de.charlex.compose.HtmlText
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.BackupContainer
import fr.simon.marquis.preferencesmanager.model.BackupContainerInfo
import fr.simon.marquis.preferencesmanager.model.PreferenceFile

@Composable
fun DialogTheme(
    openDialog: Boolean,
    icon: ImageVector? = null,
    initialSelection: Int,
    negativeText: String,
    onDismiss: () -> Unit,
    onNegative: () -> Unit,
    onPositive: (value: Int) -> Unit,
    positiveText: String,
    title: String
) {
    if (!openDialog) {
        return
    }

    // This is heavily coupled with PrefManager.themePreference
    val themeItems = listOf(
        stringResource(id = R.string.system_theme),
        stringResource(id = R.string.light_theme),
        stringResource(id = R.string.dark_theme)
    )

    var selectedItem by remember {
        mutableStateOf(initialSelection)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { icon?.let { Icon(imageVector = it, contentDescription = null) } },
        title = { Text(text = title) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .heightIn(100.dp, 250.dp)
                    .fillMaxWidth()
            ) {
                themeItems.forEachIndexed { index, item ->
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .selectable(
                                    selected = (selectedItem == index),
                                    onClick = { selectedItem = index },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                modifier = Modifier.padding(end = 16.dp),
                                selected = (selectedItem == index),
                                onClick = null
                            )
                            Text(text = item)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onPositive(selectedItem) }) {
                Text(text = positiveText)
            }
        },
        dismissButton = {
            TextButton(onClick = { onNegative() }) {
                Text(text = negativeText)
            }
        }
    )
}

@Composable
fun DialogAbout(
    openDialog: Boolean,
    onPositive: () -> Unit
) {
    if (!openDialog) {
        return
    }

    val context = LocalContext.current
    val appVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val flags = PackageManager.PackageInfoFlags.of(0L)
        context.packageManager.getPackageInfo(context.packageName, flags).versionName
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }
    val appTitle = stringResource(R.string.app_name_and_version, appVersion)

    AlertDialog(
        icon = {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher),
                contentDescription = null
            )
        },
        title = { Text(text = appTitle) },
        text = {
            val color = LocalContentColor.current
            HtmlText(color = color, textId = R.string.about_body)
        },
        onDismissRequest = onPositive,
        confirmButton = {
            TextButton(onClick = onPositive) {
                Text(text = stringResource(id = R.string.close))
            }
        }
    )
}

@Composable
fun DialogNoRoot(
    openDialog: Boolean,
    onPositive: () -> Unit
) {
    if (!openDialog) {
        return
    }

    AlertDialog(
        icon = {
            Image(
                painter = painterResource(id = R.drawable.ic_action_emo_evil),
                contentDescription = null
            )
        },
        title = { Text(text = stringResource(id = R.string.no_root_title)) },
        text = { Text(text = stringResource(id = R.string.no_root_message)) },
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onPositive) {
                Text(text = stringResource(id = R.string.no_root_button))
            }
        }
    )
}

@Composable
fun DialogRestore(
    openDialog: Boolean,
    container: BackupContainer?,
    onDelete: (file: String) -> Unit,
    onRestore: (file: String) -> Unit,
    onNegative: () -> Unit
) {
    if (!openDialog) {
        return
    }

    if (container == null || container.backupList.isEmpty()) {
        onNegative()
        return
    }

    var selectedItem: BackupContainerInfo? by remember {
        mutableStateOf(null)
    }

    AlertDialog(
        modifier = Modifier
            .padding(28.dp)
            .wrapContentHeight(),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onNegative,
        title = { Text(text = stringResource(id = R.string.pick_restore)) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .heightIn(100.dp, 250.dp)
                    .fillMaxWidth()
            ) {
                container.backupList.forEach { value ->
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .selectable(
                                    selected = (selectedItem == value),
                                    onClick = { selectedItem = value },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                modifier = Modifier.padding(end = 16.dp),
                                selected = (selectedItem == value),
                                onClick = null
                            )
                            Text(
                                modifier = Modifier.weight(1f),
                                text = value.timeSinceBackup()
                            )

                            IconButton(
                                onClick = {
                                    val file = value.backupFile
                                    onDelete(file)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedItem != null,
                onClick = { onRestore(selectedItem!!.backupFile) }
            ) {
                Text(text = stringResource(id = R.string.dialog_restore))
            }
        },
        dismissButton = {
            TextButton(onClick = onNegative) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        }
    )
}

@Composable
fun DialogEditPreference(
    openDialog: Boolean,
    preferenceFile: PreferenceFile
) {
    /* TODO */
}

@Composable
fun DialogSaveChanges(
    openDialog: Boolean,
    onPositive: () -> Unit,
    onNegative: () -> Unit,
    onCancel: () -> Unit
) {
    if (!openDialog) {
        return
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(text = "Unsaved Changes") },
        text = { Text(text = stringResource(id = R.string.popup_edit_message)) },
        confirmButton = {
            TextButton(onClick = onPositive) {
                Text(text = stringResource(id = R.string.yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onNegative) {
                Text(text = stringResource(id = R.string.no))
            }
        }
    )
}
