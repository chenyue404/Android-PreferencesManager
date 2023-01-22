@file:OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)

package fr.simon.marquis.preferencesmanager.ui.preferences

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
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

@Composable
fun TabsContent(
    tabs: List<TabItem>,
    pagerState: PagerState,
    onClick: (preferenceFile: PreferenceFile?) -> Unit,
    onLongClick: (preferenceFile: PreferenceFile?) -> Unit
) {
    HorizontalPager(state = pagerState, count = tabs.size) { page ->
        tabs[page].screen(onClick, onLongClick)
    }
}

@Composable
fun Tabs(tabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()

    ScrollableTabRow(
        modifier = Modifier.fillMaxWidth(),
        selectedTabIndex = pagerState.currentPage,
        containerColor = MaterialTheme.colorScheme.surface,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, tabPositions))
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
                },
            )
        }
    }
}
