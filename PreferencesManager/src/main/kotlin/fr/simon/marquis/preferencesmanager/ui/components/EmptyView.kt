package fr.simon.marquis.preferencesmanager.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme

@Composable
fun EmptyView(
    isEmpty: Boolean,
    emptyMessage: String
) {
    if (!isEmpty) {
        return
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Card(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.empty_view),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = emptyMessage)
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview_PreferenceEmptyView() {
    AppTheme(isDarkTheme = isSystemInDarkTheme()) {
        Surface {
            EmptyView(isEmpty = true, emptyMessage = "Empty Message")
        }
    }
}
