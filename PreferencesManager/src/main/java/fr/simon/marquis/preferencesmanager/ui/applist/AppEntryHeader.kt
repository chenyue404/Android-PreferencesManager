package fr.simon.marquis.preferencesmanager.ui.applist

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import java.util.*

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
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBlue)
                .height(1.dp)
        )
    }
}

@Preview
@Composable
private fun Preview_AppEntryHeader(
    letter: String = "Some Cool App"
) {
    AppTheme(isSystemInDarkTheme()) {
        Surface {
            AppEntryHeader(letter = letter[0])
        }
    }
}

@Preview
@Composable
private fun Preview_Dark_AppEntryHeader(
    letter: String = "Some Cool App"
) {
    AppTheme(isDarkTheme = true) {
        Surface {
            AppEntryHeader(letter = letter[0])
        }
    }
}
