package fr.simon.marquis.preferencesmanager.ui.components

import android.app.Activity
import android.graphics.Color
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.vanpra.composematerialdialogs.*
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.BackupContainer
import fr.simon.marquis.preferencesmanager.util.PrefManager
import timber.log.Timber

@Composable
fun DialogTheme(dialogState: MaterialDialogState, onPositive: () -> Unit) {
    MaterialDialog(
        dialogState = dialogState,
        buttons = {
            positiveButton("Ok")
            negativeButton("Cancel")
        }
    ) {
        // This is heavily coupled with PrefManager.themePreference
        val themeItems = listOf(
            stringResource(id = R.string.light_theme),
            stringResource(id = R.string.dark_theme),
            stringResource(id = R.string.system_theme),
        )
        listItemsSingleChoice(
            list = themeItems,
            initialSelection = PrefManager.themePreference
        ) {
            PrefManager.themePreference = it
            onPositive()
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
    val appTitle = stringResource(R.string.app_name) + "\n" + appVersion

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
            val string = stringResource(id = R.string.about_body)
            AndroidView(
                modifier = Modifier,
                factory = { context -> TextView(context) },
                update = {
                    it.text = HtmlCompat.fromHtml(string, HtmlCompat.FROM_HTML_MODE_COMPACT)
                    it.setTextColor(Color.BLACK)
                }
            )
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
    onDelete: () -> Unit,
) {
    if (container == null) {
        Timber.w("BackupContainer was null when passing to DialogRestore")
        return
    }

    MaterialDialog(
        dialogState = dialogState,
        autoDismiss = true,
        buttons = {
            positiveButton(res = R.string.dialog_restore) {
            }
            negativeButton(res = R.string.dialog_cancel)
        }
    ) {
        val listState = rememberLazyListState()
        title(res = R.string.pick_restore)
        customView {
            LazyColumn(state = listState) {
                items(container.backupList) {
                    RestoreItem {
                        // TODO
                    }
                }
            }
        }
    }
}

@Composable
private fun RestoreItem(
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth(.9f),
            text = "HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH",
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, null)
        }
    }
}

@Preview
@Composable
private fun Preview_RestoreItem() {
    RestoreItem(onDelete = {})
}
