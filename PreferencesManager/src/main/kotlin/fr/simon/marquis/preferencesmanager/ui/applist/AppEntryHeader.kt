package fr.simon.marquis.preferencesmanager.ui.applist

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme
import fr.simon.marquis.preferencesmanager.ui.theme.headerBlue
import java.util.Locale

@Composable
fun AppEntryHeader(
    modifier: Modifier = Modifier,
    letter: Char
) {
    Column(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = letter.toString().uppercase(Locale.ROOT),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBlue)
                .height(1.dp)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview_AppEntryHeader() {
    AppTheme(isDarkTheme = isSystemInDarkTheme()) {
        Surface {
            AppEntryHeader(letter = 'S')
        }
    }
}
