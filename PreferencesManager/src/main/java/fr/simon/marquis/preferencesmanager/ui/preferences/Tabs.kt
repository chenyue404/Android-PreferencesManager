package fr.simon.marquis.preferencesmanager.ui.preferences

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import fr.simon.marquis.preferencesmanager.model.PreferenceFile
import kotlinx.coroutines.launch

typealias ScreenFragment = @Composable (
    onClick: (preferenceFile: PreferenceFile?) -> Unit,
    onLongClick: (preferenceFile: PreferenceFile?) -> Unit
) -> Unit

data class TabItem(
    val pkgName: String,
    val preferenceFile: PreferenceFile? = null,
    val screen: ScreenFragment = { onClick, onLongClick ->
        PreferenceFragment(
            list = preferenceFile?.list.orEmpty(),
            onClick = { onClick(preferenceFile) },
            onLongClick = { onLongClick(preferenceFile) }
        )
    }
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabsContent(
    tabs: List<TabItem>,
    pagerState: PagerState,
    onClick: (preferenceFile: PreferenceFile?) -> Unit,
    onLongClick: (preferenceFile: PreferenceFile?) -> Unit
) {
    HorizontalPager(state = pagerState) { page ->
        tabs[page].screen(onClick, onLongClick)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tabs(tabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()

    ScrollableTabRow(
        modifier = Modifier.fillMaxWidth(),
        selectedTabIndex = pagerState.currentPage,
        containerColor = MaterialTheme.colorScheme.surface,
        indicator = { tabPositions ->
            SecondaryIndicator(Modifier.pagerTabIndicatorOffset(pagerState, tabPositions))
        }
    ) {
        tabs.mapIndexed { index, tabItem ->
            Tab(
                text = { Text(tabItem.pkgName.substringAfterLast("/")) },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }
}
