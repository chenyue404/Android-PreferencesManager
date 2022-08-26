package fr.simon.marquis.preferencesmanager.ui.components

import android.app.Activity
import android.graphics.Color
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.vanpra.composematerialdialogs.*
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.util.PrefManager

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

@Suppress("DEPRECATION")
@Composable
fun DialogAbout(
    dialogState: MaterialDialogState
) {
    val context = LocalContext.current
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
