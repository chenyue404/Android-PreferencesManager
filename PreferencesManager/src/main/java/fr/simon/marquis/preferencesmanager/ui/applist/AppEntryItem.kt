@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package fr.simon.marquis.preferencesmanager.ui.applist

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import fr.simon.marquis.preferencesmanager.model.AppEntry
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme

@Composable
fun AppEntryItem(
    modifier: Modifier = Modifier,
    entry: AppEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    ListItem(
        modifier = modifier
            .height(72.dp)
            .fillMaxWidth(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        headlineText = {
            Text(
                text = entry.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            var errorState by remember { mutableStateOf(false) }
            AsyncImage(
                modifier = Modifier.size(40.dp),
                model = entry.iconUri,
                contentDescription = null,
                onState = { state ->
                    when (state) {
                        is AsyncImagePainter.State.Error -> errorState = true
                        is AsyncImagePainter.State.Success -> errorState = false
                        else -> Unit
                    }
                }
            )

            if (errorState) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    )
}

@Preview
@Composable
private fun Preview_AppEntryItem() {
    val appInfo = ApplicationInfo().apply {
        packageName = "Some Cool App"
        name = "Some Cool App"
        sourceDir = ""
    }
    val context = LocalContext.current
    val entry = AppEntry(appInfo, context)

    AppTheme {
        AppEntryItem(entry = entry, onClick = {}, onLongClick = {})
    }
}
