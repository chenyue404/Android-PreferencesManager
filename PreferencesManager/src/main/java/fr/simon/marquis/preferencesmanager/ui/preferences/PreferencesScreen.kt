package fr.simon.marquis.preferencesmanager.ui.preferences

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.PreferenceItem
import fr.simon.marquis.preferencesmanager.ui.components.EmptyView

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PreferenceFragment(
    list: List<PreferenceItem>,
    onClick: (item: PreferenceItem) -> Unit,
    onLongClick: (item: PreferenceItem) -> Unit
) {
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scrollState = rememberLazyListState()

    Column(
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        Box(contentAlignment = Alignment.Center) {
            EmptyView(
                isEmpty = list.isEmpty(),
                emptyMessage = stringResource(id = R.string.empty_preference_file_valid)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = scrollState,
                contentPadding = WindowInsets.navigationBars.asPaddingValues()
            ) {
                items(items = list, key = { it.key }) { item ->
                    PreferencesEntryItem(
                        modifier = Modifier.animateItemPlacement(),
                        item = item,
                        onClick = { onClick(item) },
                        onLongClick = { onLongClick(item) }
                    )
                }
            }
        }
    }
}
