package fr.simon.marquis.preferencesmanager.ui.applist

import android.content.pm.ApplicationInfo
import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import fr.simon.marquis.preferencesmanager.model.AppEntry
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppEntryItem(
    modifier: Modifier = Modifier,
    entry: AppEntry,
    onClick: (entry: AppEntry) -> Unit,
    onLongClick: (entry: AppEntry) -> Unit
) {
    ListItem(
        modifier = modifier
            .height(72.dp)
            .fillMaxWidth(1f)
            .combinedClickable(
                onClick = { onClick(entry) },
                onLongClick = { onLongClick(entry) }
            ),
        headlineContent = {
            Text(
                text = entry.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = entry.applicationInfo.packageName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            SubcomposeAsyncImage(
                modifier = Modifier.size(40.dp),
                model = entry.iconUri,
                contentDescription = null,
                loading = {
                    CircularProgressIndicator()
                },
                error = {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            )
        }
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview_AppEntryItem() {
    val appInfo = ApplicationInfo().apply {
        packageName = "some.cool.app"
        name = "Some Cool App"
        sourceDir = ""
    }
    val context = LocalContext.current
    val entry = AppEntry(appInfo, context)

    AppTheme(isSystemInDarkTheme()) {
        AppEntryItem(entry = entry, onClick = {}, onLongClick = {})
    }
}
