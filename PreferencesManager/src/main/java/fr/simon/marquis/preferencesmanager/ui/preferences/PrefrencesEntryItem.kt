package fr.simon.marquis.preferencesmanager.ui.preferences

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.simon.marquis.preferencesmanager.model.KeyValueIndex
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme
import fr.simon.marquis.preferencesmanager.ui.theme.getColorFromObjet

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreferencesEntryItem(
    modifier: Modifier = Modifier,
    item: KeyValueIndex,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            .heightIn(0.dp, 110.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = getColorFromObjet(item.value)
        )
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = item.key.toString(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.value.toString(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
        }
    }
}

@Preview
@Composable
private fun Preview_PreferencesEntryItem() {
    val preferences = listOf(
        KeyValueIndex(
            key = "Hello World",
            value = "38lghsdfjlkghsfdljghsdfljkhsdflkjghsdflkjghsdlfkjghdsfkjhldfksghdfslkjgh"
        )
    )

    AppTheme(isDarkTheme = isSystemInDarkTheme()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            PreferencesEntryItem(item = preferences.first())
        }
    }
}
