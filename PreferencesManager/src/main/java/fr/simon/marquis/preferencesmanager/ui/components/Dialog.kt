package fr.simon.marquis.preferencesmanager.ui.components

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.vanpra.composematerialdialogs.*
import de.charlex.compose.HtmlText
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.BackupContainer
import fr.simon.marquis.preferencesmanager.model.PreferenceFile

@Composable
fun DialogTheme(
    dialogState: MaterialDialogState,
    initialSelection: Int,
    onPositive: (it: Int) -> Unit,
) {
    MaterialDialog(
        dialogState = dialogState,
        buttons = {
            positiveButton("Ok")
            negativeButton("Cancel")
        }
    ) {
        // This is heavily coupled with PrefManager.themePreference
        val themeItems = listOf(
            stringResource(id = R.string.system_theme),
            stringResource(id = R.string.light_theme),
            stringResource(id = R.string.dark_theme),
        )
        listItemsSingleChoice(
            list = themeItems,
            initialSelection = initialSelection
        ) {
            onPositive(it)
        }
    }
}

@Composable
fun DialogAbout(
    dialogState: MaterialDialogState
) {
    val context = LocalContext.current

    @Suppress("DEPRECATION")
    val appVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName
    val appTitle = stringResource(R.string.app_name_and_version, appVersion)

    MaterialDialog(
        dialogState = dialogState,
        buttons = {
            positiveButton(res = R.string.close)
        }
    ) {
        iconTitle(
            icon = {
                Image(
                    painterResource(id = R.drawable.ic_launcher),
                    contentDescription = null,
                )
            },
            text = appTitle
        )

        customView {
            HtmlText(textId = R.string.about_body)
        }
    }
}

@Composable
fun DialogNoRoot(
    dialogState: MaterialDialogState
) {
    val context = LocalContext.current
    MaterialDialog(
        dialogState = dialogState,
        autoDismiss = false,
        buttons = {
            positiveButton(res = R.string.no_root_button) {
                (context as Activity).finish()
            }
        },
    ) {
        iconTitle(
            icon = {
                Image(
                    painterResource(id = R.drawable.ic_action_emo_evil),
                    contentDescription = null,
                )
            },
            textRes = R.string.no_root_title
        )
        message(res = R.string.no_root_message)
    }
}

@Composable
fun DialogRestore(
    dialogState: MaterialDialogState,
    container: BackupContainer?,
    onDelete: (file: String) -> Unit,
    onRestore: (file: String) -> Unit,
) {
    if (container == null)
        return

    if (container.backupList.isEmpty())
        dialogState.hide()

    MaterialDialog(
        dialogState = dialogState,
        autoDismiss = true,
        buttons = {
            positiveButton(res = R.string.dialog_restore)
            negativeButton(res = R.string.dialog_cancel)
        }
    ) {
        title(res = R.string.pick_restore)
        listItemsCustomSingleChoice(
            list = container.backupList.map { it.timeSinceBackup() },
            onChoiceDelete = {
                val file = container.backupList[it].backupFile
                onDelete(file)
            },
            onChoiceChange = {
                val file = container.backupList[it].backupFile
                onRestore(file)
            }
        )
    }
}

@Composable
fun DialogEditPreference(
    dialogState: MaterialDialogState,
    preferenceFile: PreferenceFile,
) {
    /* TODO */
}

/**
 * Custom "Single Item List" but with a custom view.
 */
@Suppress("ComposableNaming")
@Composable
private fun MaterialDialogScope.listItemsCustomSingleChoice(
    list: List<String>,
    state: LazyListState = rememberLazyListState(),
    disabledIndices: Set<Int> = setOf(),
    initialSelection: Int? = null,
    waitForPositiveButton: Boolean = true,
    onChoiceDelete: (selected: Int) -> Unit = {},
    onChoiceChange: (selected: Int) -> Unit = {},
) {
    var selectedItem by remember { mutableStateOf(initialSelection) }
    PositiveButtonEnabled(valid = selectedItem != null) {}

    if (waitForPositiveButton) {
        DialogCallback { onChoiceChange(selectedItem!!) }
    }

    val onSelect = { index: Int ->
        if (index !in disabledIndices) {
            selectedItem = index

            if (!waitForPositiveButton) {
                onChoiceChange(selectedItem!!)
            }
        }
    }

    val onDelete = { index: Int ->
        onChoiceDelete(index)
    }

    val isEnabled = remember(disabledIndices) { { index: Int -> index !in disabledIndices } }
    listItems(
        list = list,
        state = state,
        closeOnClick = false,
        onClick = { index, _ -> onSelect(index) },
        isEnabled = isEnabled
    ) { index, item ->
        val enabled = remember(disabledIndices) { index !in disabledIndices }
        val selected = remember(selectedItem) { index == selectedItem }

        CustomViewChoiceItem(
            item = item,
            index = index,
            selected = selected,
            enabled = enabled,
            onSelect = onSelect,
            onDelete = onDelete,
        )
    }
}
